package com.skash.forge.domain.repository

import com.skash.forge.domain.model.DummyPost
import com.skash.forge.network.response.ApiResponse

interface DummyRepository {

    suspend fun fetchDummyPosts(): ApiResponse<List<DummyPost>>
}