package com.skash.forge.network.ktor

import io.ktor.client.engine.js.Js

internal actual fun getHttpClientEngineFactory(): io.ktor.client.engine.HttpClientEngineFactory<*> = Js