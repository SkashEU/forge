package com.skash.forge

import com.skash.forge.navigation.NavResultKey

sealed class NavigationResult<T>(
    key: String,
) : NavResultKey<T>(key) {

    data object ExampleNavResult : NavigationResult<Int>("example_nav_result")
}