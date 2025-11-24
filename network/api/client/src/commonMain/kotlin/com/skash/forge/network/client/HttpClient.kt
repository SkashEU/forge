package com.skash.forge.network.client

import com.skash.forge.network.response.ApiResponse
import com.skash.forge.network.response.RawResponse
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface HttpClient {
    suspend fun <Input : Any, Output : Any> execute(
        dtoClass: KClass<Input>,
        dtoType: KType,
        mapper: (Input) -> Output,
        requestBuilder: ApiRequestBuilder.() -> Unit,
    ): ApiResponse<Output>

    suspend fun executeRaw(
        requestBuilder: ApiRequestBuilder.() -> Unit
    ): ApiResponse<RawResponse>
}

suspend inline fun <reified Input : Any, Output : Any> HttpClient.execute(
    noinline requestBuilder: ApiRequestBuilder.() -> Unit,
    noinline mapper: (Input) -> Output,
): ApiResponse<Output> = this.execute( dtoClass = Input::class, typeOf<Input>(), mapper, requestBuilder)
