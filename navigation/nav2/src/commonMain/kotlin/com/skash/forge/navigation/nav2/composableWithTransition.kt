package com.skash.forge.navigation.nav2

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KType

inline fun <reified T : Any> NavGraphBuilder.composableWithTransition(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = DefaultNavTransitions.enterTransition,
        exitTransition = DefaultNavTransitions.exitTransition,
        popEnterTransition = DefaultNavTransitions.popEnterTransition,
        popExitTransition = DefaultNavTransitions.popExitTransition,
        content = content,
    )
}

@PublishedApi
internal expect object DefaultNavTransitions {
    val enterTransition:
            AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition

    val exitTransition:
            AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition

    val popEnterTransition:
            AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition

    val popExitTransition:
            AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
}