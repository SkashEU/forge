package com.skash.forge.outcome

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

suspend fun <S : Any, E> Flow<Outcome<S, E>>.collectOutcome(
    progressDelay: Long = 500L,
    onProgress: () -> Unit = {},
    onSuccess: (data: S) -> Unit = {},
    onFailure: (error: E) -> Unit = {},
) {
    handleOutcomeCollection(
        progressDelay = progressDelay,
        onProgress = onProgress,
        onSuccess = onSuccess,
        onFailure = onFailure,
    )
}

fun <S : Any, E> Flow<Outcome<S, E>>.onEachOutcome(
    progressDelay: Long = 500L,
    onProgress: () -> Unit = {},
    onSuccess: (data: S) -> Unit = {},
    onFailure: (error: E) -> Unit = {},
): Flow<Outcome<S, E>> =
    flow {
        handleOutcomeCollection(
            progressDelay = progressDelay,
            onProgress = onProgress,
            onSuccess = onSuccess,
            onFailure = onFailure,
        ) { outcome ->
            emit(outcome)
        }
    }

@OptIn(ExperimentalCoroutinesApi::class)
inline fun <I : Any, E, O : Any> Flow<Outcome<I, E>>.flatMapLatestSuccess(
    crossinline transform: suspend (value: I) -> Flow<Outcome<O, E>>,
): Flow<Outcome<O, E>> =
    this.flatMapLatest { outcome ->
        when (outcome) {
            is Outcome.Progress -> flowOf(Outcome.progress(outcome.message))
            is Outcome.Failure -> flowOf(Outcome.failure(outcome.error))
            is Outcome.Success -> transform(outcome.data)
        }
    }

inline fun <I : Any, E, O : Any> Flow<Outcome<I, E>>.mapSuccess(crossinline transform: suspend (value: I) -> O): Flow<Outcome<O, E>> =
    this.map { outcome ->
        when (outcome) {
            is Outcome.Progress -> {
                Outcome.progress(message = outcome.message)
            }

            is Outcome.Failure -> {
                Outcome.failure(outcome.error)
            }

            is Outcome.Success -> {
                val transformedData = transform(outcome.data)
                Outcome.success(transformedData)
            }
        }
    }


/**
 * Collects the Outcome from the flow and maps the outcome to its related function
 * @param progressDelay The amount of time in MS before the functions emits the [onProgress] callback to avoid showing a loading UI for a short amount of time which could look like screen flickering
 */
private suspend fun <S : Any, E> Flow<Outcome<S, E>>.handleOutcomeCollection(
    progressDelay: Long = 500L,
    onProgress: () -> Unit = {},
    onSuccess: (data: S) -> Unit = {},
    onFailure: (error: E) -> Unit = {},
    onOutcomeCollected: suspend (Outcome<S, E>) -> Unit = {},
) {
    coroutineScope {
        var progressJob: Job? = null

        collect { outcome ->
            progressJob?.cancel()

            when (outcome) {
                is Outcome.Progress -> {
                    progressJob =
                        launch {
                            delay(progressDelay)
                            onProgress()
                        }
                }

                is Outcome.Success -> onSuccess(outcome.data)
                is Outcome.Failure -> onFailure(outcome.error)
            }
            onOutcomeCollected(outcome)
        }
    }
}