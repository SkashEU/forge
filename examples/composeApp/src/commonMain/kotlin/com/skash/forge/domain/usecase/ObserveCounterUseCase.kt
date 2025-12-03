package com.skash.forge.domain.usecase

import com.skash.forge.AppDataEntry
import com.skash.forge.datastore.DataStore
import com.skash.forge.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow

class ObserveCounterUseCase(
    private val dataStore: DataStore
): FlowUseCase<Unit, Int>() {

    override fun execute(params: Unit): Flow<Int> = dataStore.observe(AppDataEntry.Count)
}