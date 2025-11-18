package com.skash.forge.data.network


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DummyPostResponse(
    @SerialName("posts")
    val posts: List<DummyPostItemResponse>
)