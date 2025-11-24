package com.skash.forge.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DefaultEventBus<T : Any> : EventBus<T> {
    private val _events =
        MutableSharedFlow<T>(
            extraBufferCapacity = 5,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    override val events = _events.asSharedFlow()

    override suspend fun sendEvent(event: T) {
        _events.emit(event)
    }
}
