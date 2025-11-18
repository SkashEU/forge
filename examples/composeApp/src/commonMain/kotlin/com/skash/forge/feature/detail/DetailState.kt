package com.skash.forge.feature.detail

sealed interface DetailState {

    sealed interface Intent

    object Loading : DetailState

    data class Details(val count: Int) : DetailState {
        sealed interface Intent: DetailState.Intent {
            data object NavigateBack : Intent
            data object IncreaseCounter : Intent
            data object DecreaseCounter : Intent
        }
    }
}