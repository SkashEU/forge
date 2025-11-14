package com.skash.forge.navigation

data class NavOptions(
    val popUpTo: NavDestination? = null,
    val popUpToInclusive: Boolean = false,
    val launchSingleTop: Boolean = true,
)