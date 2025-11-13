package com.skash.forge.network.session

import com.skash.forge.network.client.HttpClient

interface TokenAuthenticator {
    suspend fun loadTokens(httpClient: HttpClient): AuthTokens?

    suspend fun refreshTokens(httpClient: HttpClient): AuthTokens?

    suspend fun clearToken()
}
