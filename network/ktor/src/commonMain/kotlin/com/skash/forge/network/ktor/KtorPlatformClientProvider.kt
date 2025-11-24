package com.skash.forge.network.ktor

import io.ktor.client.engine.HttpClientEngineFactory

internal expect fun getHttpClientEngineFactory(): HttpClientEngineFactory<*>