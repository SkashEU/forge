package com.skash.forge.navigation.nav2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.skash.forge.navigation.NavigationDispatcher
import com.skash.forge.navigation.NavigationEvent

@Composable
fun NavController.CollectNavigationEvents(
    dispatcher: NavigationDispatcher,
    lifecycleState: Lifecycle.State = Lifecycle.State.RESUMED
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(dispatcher, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            dispatcher.events.collect { event ->
                handleNavigationEvent(event)
            }
        }
    }
}

private fun NavController.handleNavigationEvent(event: NavigationEvent) {
    when (event) {
        is NavigationEvent.NavigateTo<*> -> {
            navigate(event.destination) {
                val options = event.options ?: return@navigate
                launchSingleTop = options.launchSingleTop
                options.popUpTo?.let { popUpDest ->
                    popUpTo(popUpDest) {
                        inclusive = options.popUpToInclusive
                    }
                }
            }
        }
        is NavigationEvent.NavigateUpWithResult<*> -> {
            processNavigationResult(event)
            navigateUp()
        }
        NavigationEvent.NavigateUp -> {
            navigateUp()
        }
    }
}

private fun NavController.processNavigationResult(event: NavigationEvent.NavigateUpWithResult<*>) {
    val stackEntry = previousBackStackEntry ?: return
    val key = event.key.key
    val serializer = event.key.serializer

    val valueToSave = if (serializer != null) {
        encodeToJson(serializer, event.value)
    } else {
        event.value
    }

    stackEntry.savedStateHandle[key] = valueToSave
}