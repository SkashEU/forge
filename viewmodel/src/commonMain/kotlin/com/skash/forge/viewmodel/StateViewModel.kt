package com.skash.forge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skash.forge.event.EventBus
import com.skash.forge.logger.DefaultLogger
import com.skash.forge.logger.Logger
import com.skash.forge.logger.i
import com.skash.forge.navigation.NavigationDispatcher
import com.skash.forge.navigation.NavigationEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Abstract base ViewModel for state management
 *
 * @param State The type of state this ViewModel manages. Must be immutable.
 * @param Intent The type of user actions/intents this ViewModel processes.
 * @property initialState The initial state of the ViewModel. Likely a Loading state
 * @property eventBus Optional event bus to publish events of [Event]
 * @property stateCacheSize Maximum number of states to keep in cache (default: 10).
 * @property logger Logger instance the viewmodel uses.
 *
 *
 * ## Usage Example:
 * ```
 * class CounterViewModel : StateViewModel<CounterState, CounterState.Intent>(CounterState()) {
 *
 *     override fun executeIntent(intent: CounterState.Intent) {
 *         when (intent) {
 *             is CounterState.Intent.Decrement -> handleIntent<_, _>(intent, ::handleDecrementIntent)
 *             is CounterState.Intent.Increment -> handleIntent<_, _>(intent, ::handleIncrementIntent)
 *         }
 *     }
 *
 *     private fun handleIncrementIntent(
 *         state: CounterState,
 *         intent: CounterState.Intent.Increment
 *     ) {
 *         reduceState<CounterState> { copy(count = count + 1) }
 *     }
 *
 *     private fun handleDecrementIntent(
 *         state: CounterState,
 *         intent: CounterState.Intent.Decrement
 *     ) {
 *         reduceState<CounterState> { copy(count = count - 1) }
 *     }
 * }
 * ```
 */

abstract class NavResultAwareStateViewModel<State : Any, Intent, Event : Any, NavResult : Any>(
    initialState: State,
    eventBus: EventBus<Event>? = null,
    navigationDispatcher: NavigationDispatcher,
    stateCacheSize: Int = 10,
    logger: Logger = DefaultLogger,
) : StateViewModel<State, Intent, Event>(
    initialState,
    eventBus,
    navigationDispatcher,
    stateCacheSize,
    logger,
) {
    abstract fun onNavResultReceived(event: NavResult)
}

abstract class StateViewModel<State : Any, Intent, Event : Any>(
    initialState: State,
    private val eventBus: EventBus<Event>? = null,
    private val navigationDispatcher: NavigationDispatcher,
    private val stateCacheSize: Int = 10,
    private val logger: Logger = DefaultLogger,
) : ViewModel() {
    /**
     * The current mutable state holder. Use [collectStateFlow] to observe changes.
     */
    private val state = MutableStateFlow(initialState)

    /**
     * Read-only access to the current state value.
     */
    protected val currentState: State get() = state.value

    private val _events =
        MutableSharedFlow<Event>(
            replay = 0,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
            extraBufferCapacity = 5,
        )

    /**
     * Cache for states (max size = [stateCacheSize]).
     * This is mainly used by [resolveState] to restore old states
     */
    @PublishedApi
    internal val stateCache: MutableMap<KClass<out State>, State> = lruCache(stateCacheSize)

    /**
     * Flow of UI events. Observe this for one time events like snackbar messages or other types of [Event]
     */
    val events: SharedFlow<Event> = _events.asSharedFlow()

    /**
     * Provides the state for to observe
     * @return Cold [StateFlow] that emits state updates.
     */
    fun collectStateFlow(): StateFlow<State> = state.asStateFlow()

    /**
     * Updates the current state and optionally caches it.
     * @param state State to set
     * @param cacheState If true, caches state in [stateCache] (default: true)
     */
    protected fun setState(
        state: State,
        cacheState: Boolean = true,
    ) {
        viewModelScope.launch {
            logger.i("${this@StateViewModel::class}") { "State Update :: $state" }
            this@StateViewModel.state.update { state }

            if (cacheState) {
                cacheState(currentState)
            }
        }
    }

    /**
     * Dispatches a [NavigationEvent] to the central dispatcher.
     * @param event The navigation event to perform.
     */
    protected fun dispatchNavigationEvent(event: NavigationEvent) {
        viewModelScope.launch {
            navigationDispatcher.dispatch(event)
        }
    }

    /**
     * Entry point for intent handling. Must be implemented by specific ViewModel
     * @param intent The intent to handle.
     */
    abstract fun executeIntent(intent: Intent)

    /**
     * Type-safe state reducer. Applies transformations to the current state.
     * @param S The exact state type expected for this reduction.
     * @param reducer Transformation function that produces a new state.
     * @throws IllegalStateException if current state doesn't match type [S]
     */
    protected inline fun <reified S : State> reduceState(crossinline reducer: S.() -> State) {
        val newState =
            (currentState as? S)?.reducer() ?: run {
                error(
                    "reduceState failed: Current state: ${currentState::class.simpleName} is not of expected type ${S::class.simpleName}",
                )
            }
        setState(newState)
    }

    /**
     * Type-safe intent handler. Only processes the intent if current state of the viewmodel is the expected one.
     * @param S The required state the ViewModel needs to be in for the intent to get handled.
     * @param I The type of intent to handle
     * @param intent The intent to process
     * @param handler Function that processes the intent. It receives the required state and intent
     */
    protected inline fun <reified S : State, reified I : Intent> handleIntent(
        intent: I,
        noinline handler: (S, I) -> Unit,
    ) = (currentState as? S)?.let { handler(it, intent) } ?: handleInvalidState(intent)

    /**
     * Function to create states from cached state of type [S]
     * @param S Type of state to restore from the [stateCache].
     * @param stateFactory Factory function that combines the input data and cached state to construct a new one.
     * @throws IllegalStateException if no cached state exists for type [S].
     */
    protected inline fun <reified S : State> resolveState(stateFactory: S.() -> S = { this }) {
        val cachedState =
            stateCache[S::class] as? S ?: run {
                error("Cant resolve state, no state for type ${S::class.simpleName} cached")
            }
        setState(stateFactory(cachedState))
    }

    /**
     * Handles invalid state-intent combinations. Override for custom handling
     * @param intent The intent that couldn't get processed.
     * @throws IllegalStateException in debug builds by default
     */
    protected open fun handleInvalidState(intent: Intent) {
        error("Invalid state ${currentState::class.simpleName} for intent ${intent!!::class.simpleName}")
    }

    /**
     * Emits a UI event to either the event bus or direct flow. Depends on [eventBus].
     * @param event The UI event to emit, for example to display a snackbar message.
     */
    protected fun sendUIEvent(event: Event) {
        viewModelScope.launch {
            eventBus?.sendEvent(event) ?: _events.emit(event)
        }
    }

    /**
     * Stores the given state in the [stateCache]
     * @param state The state instance to cache (keyed by its class)
     */
    private fun cacheState(state: State) {
        stateCache[state::class] = state
    }
}
