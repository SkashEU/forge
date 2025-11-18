package com.skash.forge.feature.main

import com.skash.forge.domain.model.DummyPost

sealed interface MainState {

    sealed interface Intent

    data object Loading : MainState

    data class Main(
        val count: Int = 0,
        val posts: List<DummyPost> = emptyList()
    ) : MainState {

        sealed interface Intent: MainState.Intent {
            data object NavigateToDetails : MainState.Intent
        }
    }
}
