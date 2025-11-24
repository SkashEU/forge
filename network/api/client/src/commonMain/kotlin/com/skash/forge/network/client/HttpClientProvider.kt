package com.skash.forge.network.client

interface StateClearable {
    suspend fun clearState()
}

data class HttpClientBundle(
    val client: HttpClient,
    val stateClearable: StateClearable,
)