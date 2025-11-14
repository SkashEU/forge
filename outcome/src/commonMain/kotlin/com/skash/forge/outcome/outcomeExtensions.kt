package com.skash.forge.outcome

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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