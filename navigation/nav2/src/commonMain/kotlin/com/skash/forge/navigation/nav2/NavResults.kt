package com.skash.forge.navigation.nav2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import com.skash.forge.navigation.NavResultKey

@Composable
fun <I : Any> HandleNavResults(
    handle: SavedStateHandle?,
    onResult: (I) -> Unit,
    builder: @Composable NavResultHandlerScope<I>.() -> Unit,
) {
    val scope =
        remember(handle, onResult) {
            NavResultHandlerScope(handle, onResult)
        }
    scope.builder()
}

class NavResultHandlerScope<I : Any>(
    internal val handle: SavedStateHandle?,
    internal val onIntent: (I) -> Unit,
)

@Composable
fun <T : Any, I : Any> NavResultHandlerScope<I>.OnResult(
    navResult: NavResultKey<T>,
    mapper: (value: T) -> I,
) {
    HandleNavResultAsIntent(
        handle = handle,
        navResult = navResult,
        mapper = mapper,
        onIntent = onIntent,
    )
}

@Composable
fun <T : Any, I : Any> HandleNavResultAsIntent(
    handle: SavedStateHandle?,
    navResult: NavResultKey<T>,
    mapper: (value: T) -> I,
    onIntent: (I) -> Unit,
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
fun <T : Any> SavedStateHandle?.rememberNavResultAsEvent(navResult: NavResultKey<T>): State<T?> {
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