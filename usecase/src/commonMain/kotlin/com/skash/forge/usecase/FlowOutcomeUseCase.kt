package com.skash.forge.usecase

import com.skash.forge.network.response.ApiResponse
import com.skash.forge.outcome.Outcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

abstract class FlowOutcomeUseCase<in Params, S : Any, E>(
    dispatcher: CoroutineDispatcher = defaultDispatcher,
    private val emitProgressOnStart: Boolean = true
) : FlowUseCase<Params, Outcome<S, E>>(dispatcher) {

    protected abstract suspend fun FlowCollector<Outcome<S, E>>.execute(params: Params)

    final override fun execute(params: Params): Flow<Outcome<S, E>> = flow {
        execute(params)
    }

    override operator fun invoke(params: Params): Flow<Outcome<S, E>> =
        super.invoke(params).let { flow ->
            when (emitProgressOnStart) {
                true -> flow.onStart { emit(Outcome.progress()) }
                false -> flow
            }
        }

    protected suspend fun FlowCollector<Outcome<S, E>>.emitSuccess(data: S) {
        emit(Outcome.success(data = data))
    }

    protected suspend fun FlowCollector<Outcome<S, E>>.emitFailure(error: E) {
        emit(Outcome.failure(error = error))
    }

    protected suspend fun FlowCollector<Outcome<S, E>>.emitFrom(
        apiResponse: ApiResponse<S>,
        errorMapper: ApiResponse.Error.() -> E
    ) {
        when (apiResponse) {
            is ApiResponse.Error -> emitFailure(errorMapper(apiResponse))
            is ApiResponse.Success -> emitSuccess(apiResponse.body)
        }
    }

    protected suspend fun FlowCollector<Outcome<S, E>>.emitCatching(
        errorMapper: (Throwable) -> E,
        block: suspend () -> S
    ) = runCatching {
        emitSuccess(block())
    }.exceptionOrNull()?.let { exception ->
        when (exception) {
            is CancellationException -> throw exception
            else -> emitFailure(errorMapper(exception))
        }
    }
}

operator fun <Output: Any, Error> FlowOutcomeUseCase<Unit, Output, Error>.invoke(): Flow<Outcome<Output, Error>> = invoke(Unit)