package com.skash.forge

sealed interface UIEvent {
    data class Snackbar(val message: String) : UIEvent
}