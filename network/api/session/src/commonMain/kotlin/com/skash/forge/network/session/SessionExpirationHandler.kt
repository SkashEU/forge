package com.skash.forge.network.session

interface SessionExpirationHandler {
    suspend fun onSessionExpired()
}
