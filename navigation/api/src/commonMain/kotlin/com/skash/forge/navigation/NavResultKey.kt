package com.skash.forge.navigation

import kotlinx.serialization.KSerializer

open class NavResultKey<T>(
    val key: String,
    val serializer: KSerializer<T>? = null,
)