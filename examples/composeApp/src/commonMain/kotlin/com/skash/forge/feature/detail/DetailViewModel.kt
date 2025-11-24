package com.skash.forge.feature.detail

import androidx.lifecycle.viewModelScope
import com.skash.forge.BaseViewModel
import com.skash.forge.NavigationResult
import com.skash.forge.domain.usecase.DecreaseCounterUseCase
import com.skash.forge.domain.usecase.GetDummyPostsUseCase
import com.skash.forge.domain.usecase.IncreaseCounterUseCase
import com.skash.forge.domain.usecase.ObserveCounterUseCase
import com.skash.forge.navigation.NavigationEvent
import com.skash.forge.outcome.collectOutcome
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailViewModel(
    count: Int,
    observeCounterUseCase: ObserveCounterUseCase,
    private val increaseCounterUseCase: IncreaseCounterUseCase,
    private val decreaseCounterUseCase: DecreaseCounterUseCase
) : BaseViewModel<DetailState, DetailState.Intent>(initialState = DetailState.Details(count = count)) {

    private val currentCount = observeCounterUseCase(Unit)
        .onEach { setState(DetailState.Details(it)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    override fun executeIntent(intent: DetailState.Intent) = when (intent) {
        is DetailState.Details.Intent.DecreaseCounter -> handleIntent<_, _>(
            intent = intent,
            handler = ::handleDecreaseCounter
        )

        is DetailState.Details.Intent.IncreaseCounter -> handleIntent<_, _>(
            intent = intent,
            handler = ::handleIncreaseCounter
        )

        is DetailState.Details.Intent.NavigateBack -> handleIntent<_, _>(
            intent = intent,
            handler = ::handleNavigateUp
        )
    }

    private fun handleDecreaseCounter(
        state: DetailState.Details,
        intent: DetailState.Details.Intent.DecreaseCounter
    ) {

        viewModelScope.launch {
            decreaseCounterUseCase.invoke(DecreaseCounterUseCase.DecreaseCounterParams(state.count))
                .collectOutcome(onProgress = { setState(DetailState.Loading) })
        }
    }

    private fun handleIncreaseCounter(
        state: DetailState.Details,
        intent: DetailState.Details.Intent.IncreaseCounter
    ) {
        viewModelScope.launch {
            increaseCounterUseCase.invoke(IncreaseCounterUseCase.IncreaseCounterParams(state.count))
                .collectOutcome(onProgress = { setState(DetailState.Loading) })
        }
    }

    private fun handleNavigateUp(
        state: DetailState.Details,
        intent: DetailState.Details.Intent.NavigateBack
    ) = dispatchNavigationEvent(
        event = NavigationEvent.NavigateUpWithResult(
            key = NavigationResult.ExampleNavResult,
            value = state.count
        )
    )
}