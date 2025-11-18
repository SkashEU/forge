package com.skash.forge.navigation.nav2

import com.skash.forge.navigation.NavigationDispatcher
import com.skash.forge.navigation.NavigationEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DefaultNavigationDispatcher : NavigationDispatcher {
    private val _events = MutableSharedFlow<NavigationEvent>(
        extraBufferCapacity = 5,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val events: Flow<NavigationEvent> = _events.asSharedFlow()

    override suspend fun dispatch(event: NavigationEvent) {
        _events.emit(event)
    }
}