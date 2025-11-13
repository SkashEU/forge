package com.skash.forge.network.ktor

import io.ktor.client.engine.darwin.Darwin

internal actual fun getHttpClientEngineFactory(): io.ktor.client.engine.HttpClientEngineFactory<*> = Darwin