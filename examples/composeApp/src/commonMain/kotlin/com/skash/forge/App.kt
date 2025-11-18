package com.skash.forge

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    KoinApplication(application = {
        modules(appContainer)
    }) {
        MaterialTheme {
            AppNavigation(onNavHostReady)
        }
    }
}

