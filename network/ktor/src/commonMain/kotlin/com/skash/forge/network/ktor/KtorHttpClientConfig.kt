package com.skash.forge.network.ktor

import com.skash.forge.network.session.TokenAuthenticator
import io.ktor.http.HeadersBuilder
import kotlinx.serialization.json.Json

class KtorApiClientConfig {
    internal var tokenAuthenticator: TokenAuthenticator? = null
    internal var json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            explicitNulls = false
            coerceInputValues = true
        }
    internal var defaultHeaders: HeadersBuilder.() -> Unit = {}

    /**
     * Sets the [TokenAuthenticator] to be used for automatic token loading and refreshing.
     */
    fun authentication(authenticator: TokenAuthenticator) {
        this.tokenAuthenticator = authenticator
    }

    /**
     * Provides a custom [Json] serializer instance.
     */
    fun json(json: Json) {
        this.json = json
    }

    /**
     * Sets a block to apply default headers to every request.
     */
    fun defaultHeaders(block: HeadersBuilder.() -> Unit) {
        this.defaultHeaders = block
    }
}