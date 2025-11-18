package com.skash.forge.domain.usecase

import com.skash.forge.AppDataEntry
import com.skash.forge.datastore.DataStore
import com.skash.forge.outcome.Outcome
import com.skash.forge.usecase.OutcomeUseCase
import kotlinx.coroutines.flow.FlowCollector

class DecreaseCounterUseCase(
    private val dataStore: DataStore
): OutcomeUseCase<DecreaseCounterUseCase.DecreaseCounterParams, Unit, String>() {

    override suspend fun FlowCollector<Outcome<Unit, String>>.execute(params: DecreaseCounterParams) {
        dataStore.set(AppDataEntry.Count, params.count - 1)
        emitSuccess(Unit)
    }

    data class DecreaseCounterParams(
        val count: Int
    )
}