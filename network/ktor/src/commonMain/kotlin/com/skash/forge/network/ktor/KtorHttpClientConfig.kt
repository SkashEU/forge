package com.skash.forge.network.ktor

import com.skash.forge.logger.DefaultLogger
import com.skash.forge.logger.Logger
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
    internal var logger: Logger = DefaultLogger
    internal var logLevel : HttpLogLevel = HttpLogLevel.Body

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

    /**
     * Sets the logger that Ktor uses for logging.
     */
    fun logger(logger: Logger = DefaultLogger, level: HttpLogLevel = HttpLogLevel.Body) {
        this.logger = logger
        this.logLevel = level
    }
}