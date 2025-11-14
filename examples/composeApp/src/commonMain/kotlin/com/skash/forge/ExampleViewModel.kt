package com.skash.forge

import androidx.lifecycle.viewModelScope
import com.skash.forge.navigation.NavigationDispatcher
import com.skash.forge.navigation.NavigationEvent
import com.skash.forge.outcome.collectOutcome
import com.skash.forge.viewmodel.StateViewModel
import kotlinx.coroutines.launch

sealed interface ExampleState {
    data object Loading : ExampleState
    data class Error(val message: String) : ExampleState
    data class Success(val count: Int) : ExampleState {
        sealed interface Intent : ExampleState.Intent {
            data object Increment : Intent
            data object Decrement : Intent
        }
    }

    sealed interface Intent
}

class NavigationDispatcher : NavigationDispatcher {
    override suspend fun dispatch(event: NavigationEvent) {

    }
}

class ExampleViewModel : StateViewModel<ExampleState, ExampleState.Intent, String>(
    initialState = ExampleState.Success(1),
    navigationDispatcher = NavigationDispatcher()
) {
    override fun executeIntent(intent: ExampleState.Intent) = when (intent) {
        ExampleState.Success.Intent.Decrement -> reduceState<ExampleState.Success> {
            copy(count = count - 1)
        }

        is ExampleState.Success.Intent.Increment -> handleIntent<_, _>(intent = intent, handler = ::handleIncrement)
    }

    private fun handleIncrement(
        state: ExampleState.Success,
        intent: ExampleState.Success.Intent.Increment
    ) {

        viewModelScope.launch {
            ExampleUseCase()
                .invoke(ExampleUseCase.ExampleUseCaseParams(state.count))
                .collectOutcome(
                    onSuccess = { updatedCount ->
                        setState(state.copy(count = updatedCount))
                    },
                    onProgress = {
                        setState(ExampleState.Loading)
                    }
                )
        }
    }
}