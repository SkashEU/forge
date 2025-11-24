package com.skash.forge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.skash.forge.feature.detail.DetailViewModel
import com.skash.forge.feature.detail.DetailsScreen
import com.skash.forge.feature.main.MainNavigationResult
import com.skash.forge.feature.main.MainScreen
import com.skash.forge.feature.main.MainViewModel
import com.skash.forge.navigation.NavigationDispatcher
import com.skash.forge.navigation.nav2.CollectNavigationEvents
import com.skash.forge.navigation.nav2.DefaultNavHost
import com.skash.forge.navigation.nav2.HandleNavResults
import com.skash.forge.navigation.nav2.composableWithTransition
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavigation(
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    val rootNavController = rememberNavController()
    val navigationDispatcher = koinInject<NavigationDispatcher>()

    rootNavController.CollectNavigationEvents(navigationDispatcher)

    LaunchedEffect(rootNavController) {
        onNavHostReady(rootNavController)
    }

    DefaultNavHost(rootNavController, startDestination = AppScreen.Main) {
        composableWithTransition<AppScreen.Main> { backStackEntry ->

            val viewModel = koinViewModel<MainViewModel>()

            HandleNavResults(
                handle = backStackEntry.savedStateHandle,
                onResult = viewModel::onNavResultReceived
            ) {
                OnResult(
                    navResult = NavigationResult.ExampleNavResult,
                    mapper = { MainNavigationResult.ExampleEvent(it) }
                )
            }

            MainScreen(viewModel)
        }

        composableWithTransition<AppScreen.Details> { backStackEntry ->
            val currentCount = backStackEntry.toRoute<AppScreen.Details>().currentCount
            val viewModel = koinViewModel<DetailViewModel>(parameters = { parametersOf(currentCount) })

            DetailsScreen(viewModel)
        }
    }
}