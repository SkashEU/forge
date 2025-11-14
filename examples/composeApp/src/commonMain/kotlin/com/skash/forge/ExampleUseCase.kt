package com.skash.forge

import com.skash.forge.outcome.Outcome
import com.skash.forge.usecase.OutcomeUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector

class ExampleUseCase: OutcomeUseCase<ExampleUseCase.ExampleUseCaseParams, Int, String>() {

    override suspend fun FlowCollector<Outcome<Int, String>>.execute(
        params: ExampleUseCaseParams
    ) {
        delay(5000)
        emitSuccess(params.count + 1)
    }

    data class ExampleUseCaseParams(
        val count: Int
    )
}