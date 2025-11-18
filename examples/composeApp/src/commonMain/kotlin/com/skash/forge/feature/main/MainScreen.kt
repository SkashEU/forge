package com.skash.forge.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skash.forge.UIEvent
import com.skash.forge.domain.model.DummyPost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MainScreen(viewModel: MainViewModel) {

    val state by viewModel.collectStateFlow().collectAsState()

    MainScreenImpl(
        state = state,
        events = viewModel.events,
        executeIntent = viewModel::executeIntent
    )
}

@Composable
private fun MainScreenImpl(
    state: MainState,
    events: Flow<UIEvent>,
    executeIntent: (MainState.Intent) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(events) {
        events.collectLatest { event ->
            when (event) {
                is UIEvent.Snackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            when (state) {
                MainState.Loading -> CircularProgressIndicator()
                is MainState.Main -> MainPage(state = state, executeIntent = executeIntent)
            }
        }
    }
}

@Composable
private fun MainPage(
    state: MainState.Main,
    executeIntent: (MainState.Intent) -> Unit
) {

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "header") {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Current count: ${state.count}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = { executeIntent(MainState.Main.Intent.NavigateToDetails) }) {
                    Text(text = "Go to Details")
                }
            }
        }

        items(state.posts, key = { it.id }) {
            DummyPostCard(post = it)
        }
    }
}

@Composable
private fun DummyPostCard(post: DummyPost) {
    Card(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            Text(text = post.slug, style = MaterialTheme.typography.headlineMedium)
            Text(text = post.content)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MainScreenImpl(state = MainState.Main(), events = emptyFlow(), executeIntent = {})
}