package com.skash.forge

import com.skash.forge.datastore.DataStore
import com.skash.forge.outcome.Outcome
import com.skash.forge.usecase.OutcomeUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector

class ExampleUseCase(
    private val dataStore: DataStore
): OutcomeUseCase<ExampleUseCase.ExampleUseCaseParams, Unit, String>() {

    override suspend fun FlowCollector<Outcome<Unit, String>>.execute(
        params: ExampleUseCaseParams
    ) {
        delay(1000)
        dataStore.set(AppDataEntry.Count, params.count + 1)
        emitSuccess(Unit)
    }

    data class ExampleUseCaseParams(
        val count: Int
    )
}