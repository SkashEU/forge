package com.skash.forge.data.repository

import com.skash.forge.data.mapper.toDummyPost
import com.skash.forge.data.network.DummyPostResponse
import com.skash.forge.domain.model.DummyPost
import com.skash.forge.domain.repository.DummyRepository
import com.skash.forge.network.client.HttpClient
import com.skash.forge.network.client.execute
import com.skash.forge.network.response.ApiResponse

class DummyRepositoryImpl(
    private val httpClient: HttpClient
) : DummyRepository {
    override suspend fun fetchDummyPosts(): ApiResponse<List<DummyPost>> =
        httpClient.execute<DummyPostResponse, List<DummyPost>>(
            requestBuilder = {
                get("https://dummyjson.com/posts")
            },
            mapper = { it.posts.map { it.toDummyPost() } }
        )
}