package com.skash.forge.navigation.nav2

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * A wrapper around the standard [NavHost] that provides default cross-platform animations.
 *
 * This composable simplifies the creation of a navigation host by pre-configuring
 * enter, exit, popEnter, and popExit transitions with a consistent fade animation
 * (`fadeIn` and `fadeOut` with a 700ms duration). It acts as a drop-in replacement
 * for the standard `NavHost`, allowing for easy setup while still offering full
 * customization of all parameters if needed.
 *
 * @param navController The [NavHostController] that manages navigation within this host.
 * @param startDestination The route for the initial screen to be displayed.
 * @param modifier The [Modifier] to be applied to the `NavHost` container.
 * @param contentAlignment The alignment of the content within the `NavHost`.
 * @param route The optional route for the navigation graph itself.
 * @param typeMap A map of custom [NavType]s for argument parsing.
 * @param enterTransition The transition for a destination entering the screen.
 * **Defaults to a 700ms fade-in.**
 * @param exitTransition The transition for a destination exiting the screen.
 * **Defaults to a 700ms fade-out.**
 * @param popEnterTransition The transition for a destination re-entering the screen
 * when the back stack is popped. Defaults to [enterTransition].
 * @param popExitTransition The transition for a destination exiting the screen
 * when the back stack is popped. Defaults to [exitTransition].
 * @param sizeTransform An optional transition for handling size changes between
 * destinations.
 * @param builder A lambda function to build the navigation graph using the [NavGraphBuilder].
 * This is where you define your composable destinations.
 */
@Composable
fun DefaultNavHost(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    route: KClass<*>? = null,
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    enterTransition:
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
        {
            fadeIn(animationSpec = tween(700))
        },
    exitTransition:
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
        {
            fadeOut(animationSpec = tween(700))
        },
    popEnterTransition:
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
        enterTransition,
    popExitTransition:
    (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
        exitTransition,
    sizeTransform: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? =
        null,
    builder: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        contentAlignment = contentAlignment,
        route = route,
        typeMap = typeMap,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        sizeTransform = sizeTransform,
        builder = builder,
    )
}