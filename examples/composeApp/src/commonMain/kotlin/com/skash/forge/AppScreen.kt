package com.skash.forge

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppScreen {
    @Serializable
    data object Main: AppScreen
    @Serializable
    data class Details(val currentCount: Int): AppScreen
}