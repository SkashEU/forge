package com.skash.forge.network.ktor

import io.ktor.client.engine.cio.*

internal actual fun getHttpClientEngineFactory(): io.ktor.client.engine.HttpClientEngineFactory<*> = CIO