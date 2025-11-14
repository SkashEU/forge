package com.skash.forge.navigation

interface NavigationDispatcher {
    suspend fun dispatch(event: NavigationEvent)
}