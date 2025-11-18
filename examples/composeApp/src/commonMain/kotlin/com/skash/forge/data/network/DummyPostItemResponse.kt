package com.skash.forge.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DummyPostItemResponse(
    @SerialName("body")
    val content: String,
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val slug: String,
)