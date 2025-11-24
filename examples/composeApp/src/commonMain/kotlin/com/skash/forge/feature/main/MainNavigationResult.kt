package com.skash.forge.feature.main


sealed interface MainNavigationResult {
    data class ExampleEvent(val count: Int) : MainNavigationResult
}