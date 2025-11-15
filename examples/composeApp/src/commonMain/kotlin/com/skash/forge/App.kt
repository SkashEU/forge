package com.skash.forge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {

    KoinApplication(application = {
        modules(appContainer)
    }) {
        MaterialTheme {
            val viewModel = koinViewModel<ExampleViewModel>()
            val state by viewModel.collectStateFlow().collectAsState()

            Surface {
                when (val uiState = state) {
                    is ExampleState.Error -> {}
                    ExampleState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            LinearProgressIndicator()
                        }
                    }

                    is ExampleState.Success -> CounterPage(
                        count = uiState.count,
                        onIncrement = { viewModel.executeIntent(ExampleState.Success.Intent.Increment) },
                        onDecrement = { viewModel.executeIntent(ExampleState.Success.Intent.Decrement) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CounterPage(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(text = "Count: $count")

        Row {
            Button(onClick = onDecrement) {
                Text("Decrease")
            }

            Button(onClick = onIncrement) {
                Text("Increase")
            }
        }
    }
}