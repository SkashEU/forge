package com.skash.forge.navigation

sealed interface NavigationEvent {
    data class NavigateTo(
        val destination: NavDestination,
        val options: NavOptions? = null,
    ) : NavigationEvent

    data object NavigateUp : NavigationEvent

    data class NavigateUpWithResult<T : Any>(
        val key: NavResultKey<T>,
        val value: T,
    ) : NavigationEvent
}