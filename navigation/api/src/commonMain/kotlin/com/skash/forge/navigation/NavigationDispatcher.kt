package com.skash.forge.navigation

import kotlinx.coroutines.flow.Flow

interface NavigationDispatcher {
    val events: Flow<NavigationEvent>

    suspend fun dispatch(event: NavigationEvent)
}