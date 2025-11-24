package com.skash.forge.domain.usecase

import com.skash.forge.AppDataEntry
import com.skash.forge.datastore.DataStore
import com.skash.forge.outcome.Outcome
import com.skash.forge.usecase.OutcomeUseCase
import kotlinx.coroutines.flow.FlowCollector

class IncreaseCounterUseCase(
    private val dataStore: DataStore
): OutcomeUseCase<IncreaseCounterUseCase.IncreaseCounterParams, Unit, String>() {

    override suspend fun FlowCollector<Outcome<Unit, String>>.execute(params: IncreaseCounterParams) {
        dataStore.set(AppDataEntry.Count, params.count + 1)
        emitSuccess(Unit)
    }

    data class IncreaseCounterParams(
        val count: Int
    )
}