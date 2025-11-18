package com.skash.forge.navigation.nav2

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

private val navigationJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

@Suppress("UNCHECKED_CAST")
internal fun <T> encodeToJson(
    serializer: KSerializer<T>,
    value: Any?,
): String {
    return navigationJson.encodeToString(serializer, value as T)
}

@PublishedApi
@Suppress("UNCHECKED_CAST")
internal fun <T> decodeFromJson(
    serializer: KSerializer<T>,
    value: String,
): T {
    return navigationJson.decodeFromString(serializer, value)
}