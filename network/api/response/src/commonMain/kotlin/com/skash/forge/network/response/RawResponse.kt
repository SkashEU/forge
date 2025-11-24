package com.skash.forge.network.response

data class RawResponse(
    val status: Int,
    val headers: Map<String, List<String>>,
    val body: String,
)
