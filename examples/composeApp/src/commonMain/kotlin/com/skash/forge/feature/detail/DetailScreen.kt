package com.skash.forge.feature.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DetailsScreen(viewModel: DetailViewModel) {

    val state by viewModel.collectStateFlow().collectAsState()

    DetailsScreenImpl(
        state = state,
        executeIntent = viewModel::executeIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsScreenImpl(
    state: DetailState,
    executeIntent: (DetailState.Intent) -> Unit
) {

    Scaffold(
        topBar = {
            AnimatedVisibility(state is DetailState.Details) {
                TopAppBar(
                    title = { Text("Details") },
                    navigationIcon = {
                        IconButton(onClick = { executeIntent(DetailState.Details.Intent.NavigateBack) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

           when(state) {
               is DetailState.Details -> DetailsPage(state = state, executeIntent = executeIntent)
               DetailState.Loading -> CircularProgressIndicator()
           }
        }
    }
}

@Composable
private fun DetailsPage(
    state: DetailState.Details,
    executeIntent: (DetailState.Details.Intent) -> Unit
) {
    Text(text = "Current count: ${state.count}", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(20.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Button(onClick = { executeIntent(DetailState.Details.Intent.DecreaseCounter) }) {
            Text(text = "Decrease")
        }

        Button(onClick = { executeIntent(DetailState.Details.Intent.IncreaseCounter) }) {
            Text(text = "Increase")
        }
    }
}

@Preview
@Composable
private fun Preview() {
    DetailsScreenImpl(state = DetailState.Details(1), executeIntent = {})
}