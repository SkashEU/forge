package com.skash.forge

import com.skash.forge.datastore.DataStore
import com.skash.forge.usecase.UseCase
import kotlinx.coroutines.flow.Flow

class ObserveCounterUseCase(
    private val dataStore: DataStore
): UseCase<Unit, Int>() {

    override fun execute(params: Unit): Flow<Int> = dataStore.observe(AppDataEntry.Count)
}