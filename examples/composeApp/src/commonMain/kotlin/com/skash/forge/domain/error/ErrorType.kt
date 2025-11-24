package com.skash.forge.domain.error

sealed class ErrorType(val message: String) {
    data object PostFetchFailed: ErrorType("Failed to fetch posts")
}