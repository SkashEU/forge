package com.skash.forge.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

abstract class UseCase<in Params, out Type : Any>(
    private val dispatcher: CoroutineDispatcher = defaultDispatcher
) {

    protected abstract fun execute(params: Params): Flow<Type>

    open operator fun invoke(params: Params): Flow<Type> = execute(params).flowOn(dispatcher)
}