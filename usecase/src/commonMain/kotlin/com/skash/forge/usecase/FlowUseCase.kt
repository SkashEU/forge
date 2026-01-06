package com.skash.forge.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

abstract class FlowUseCase<in Params, out Output>(
    private val dispatcher: CoroutineDispatcher = defaultDispatcher
) {

    protected abstract fun execute(params: Params): Flow<Output>

    open operator fun invoke(params: Params): Flow<Output> = execute(params).flowOn(dispatcher)
}

operator fun <Output> FlowUseCase<Unit, Output>.invoke(): Flow<Output> = invoke(Unit)