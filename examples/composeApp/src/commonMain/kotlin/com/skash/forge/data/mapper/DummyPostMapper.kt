package com.skash.forge.data.mapper

import com.skash.forge.data.network.DummyPostItemResponse
import com.skash.forge.domain.model.DummyPost

fun DummyPostItemResponse.toDummyPost(): DummyPost = DummyPost(
    id = id,
    slug = slug,
    content = content,
)