package com.skash.forge.event

import kotlinx.coroutines.flow.SharedFlow

interface EventBus<T : Any> {
    val events: SharedFlow<T>

    suspend fun sendEvent(event: T)
}
