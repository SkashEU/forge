package com.skash.forge.network.session

data class AuthTokens(
    val bearer: String,
    val refresh: String,
)
