package com.skash.forge.navigation.nav2

import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import com.skash.forge.navigation.NavResultKey

@Composable
inline fun <I : Any> HandleNavResults(
    handle: SavedStateHandle?,
    noinline onResult: (I) -> Unit,
    builder: @Composable NavResultHandlerScope<I>.() -> Unit,
) {
    val scope = remember(handle, onResult) {
        NavResultHandlerScope(handle, onResult)
    }
    scope.builder()
}

class NavResultHandlerScope<I : Any>(
    @PublishedApi
    internal val handle: SavedStateHandle?,
    @PublishedApi
    internal val onIntent: (I) -> Unit,
) {
    @Composable
    inline fun <reified T : Any> OnResult(
        navResult: NavResultKey<T>,
        crossinline mapper: (value: T) -> I,
    ) {
        HandleNavResultAsIntent(
            handle = handle,
            navResult = navResult,
            mapper = mapper,
            onIntent = onIntent,
        )
    }
}

@Composable
inline fun <reified T : Any, I : Any> HandleNavResultAsIntent(
    handle: SavedStateHandle?,
    navResult: NavResultKey<T>,
    crossinline mapper: (value: T) -> I,
    crossinline onIntent: (I) -> Unit,
) {
    val navResultEvent by handle.rememberNavResultAsEvent(navResult)

    LaunchedEffect(navResultEvent) {
        navResultEvent?.let {
            val intent = mapper(it)
            onIntent(intent)
        }
    }
}

@Composable
inline fun <reified T : Any> SavedStateHandle?.rememberNavResultAsEvent(navResult: NavResultKey<T>): State<T?> {
    if (this == null) {
        return remember { mutableStateOf(null) }
    }
    return produceState(initialValue = null, this, navResult) {
        val key = navResult.key
        val serializer = navResult.serializer
        val valueFromHandle: T? =
            if (serializer == null) {
                this@rememberNavResultAsEvent.remove(key)
            } else {
                this@rememberNavResultAsEvent.get<String>(key)?.let { jsonString ->
                    this@rememberNavResultAsEvent.remove<String>(key)
                    decodeFromJson(serializer, jsonString)
                }
            }
        if (valueFromHandle != null) {
            this.value = valueFromHandle
        }
    }
}