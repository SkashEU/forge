package com.skash.forge.feature.main

import androidx.lifecycle.viewModelScope
import com.skash.forge.AppScreen
import com.skash.forge.BaseViewModelWithNavResult
import com.skash.forge.UIEvent
import com.skash.forge.domain.usecase.GetDummyPostsUseCase
import com.skash.forge.navigation.NavigationEvent
import com.skash.forge.outcome.collectOutcome
import com.skash.forge.outcome.onEachOutcome
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    getDummyPostsUseCase: GetDummyPostsUseCase
) : BaseViewModelWithNavResult<MainState, MainState.Intent, MainNavigationResult>(
    initialState = MainState.Loading,
    useEventBus = false
) {

    private val posts = getDummyPostsUseCase(Unit)
        .onEachOutcome(
            onSuccess = { posts ->
                reduceStateOrCreate<MainState.Main>(
                    reducer = { copy(posts = posts) },
                    create = { MainState.Main(posts = posts) }
                )
            },
            onFailure = { sendUIEvent(UIEvent.Snackbar(it.message)) }
        )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    override fun onNavResultReceived(event: MainNavigationResult) = when (event) {
        is MainNavigationResult.ExampleEvent -> reduceStateOrCreate<MainState.Main>(
            reducer = { copy(count = event.count) },
            create = { MainState.Main(count = event.count) }
        )
    }

    override fun executeIntent(intent: MainState.Intent) = when (intent) {
        is MainState.Main.Intent.NavigateToDetails -> handleIntent<_, _>(
            intent = intent,
            handler = ::handleNavigateToDetails
        )
    }

    private fun handleNavigateToDetails(
        state: MainState.Main,
        intent: MainState.Main.Intent.NavigateToDetails
    ) {
        dispatchNavigationEvent(
            NavigationEvent.NavigateTo(
                destination = AppScreen.Details(
                    currentCount = state.count
                )
            )
        )
    }
}