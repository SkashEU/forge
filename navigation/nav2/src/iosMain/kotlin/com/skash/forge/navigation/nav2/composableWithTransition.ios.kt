package com.skash.forge.navigation.nav2

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

@PublishedApi
internal actual object DefaultNavTransitions {
    actual val enterTransition:
            AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec =
                tween(
                    durationMillis = 200,
                    easing = LinearEasing,
                ),
        )
    }
    actual val exitTransition:
            AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec =
                tween(
                    durationMillis = 200,
                    easing = LinearEasing,
                ),
            targetOffset = { fullOffset -> (fullOffset * 0.3f).toInt() },
        )
    }
    actual val popEnterTransition:
            AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec =
                tween(
                    durationMillis = 200,
                    easing = LinearEasing,
                ),
            initialOffset = { fullOffset -> (fullOffset * 0.3f).toInt() },
        )
    }
    actual val popExitTransition:
            AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec =
                tween(
                    durationMillis = 200,
                    easing = LinearEasing,
                ),
        )
    }
}