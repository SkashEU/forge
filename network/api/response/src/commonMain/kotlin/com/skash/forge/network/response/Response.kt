package com.skash.forge.network.response

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

sealed class ApiResponse<out E> {
    abstract val code: Int

    data class Success<out E>(
        val body: E,
        override val code: Int = 200,
    ) : ApiResponse<E>()

    sealed class Error : ApiResponse<Nothing>() {
        override val code: Int = 500
        open val reason: String? = null

        data class HttpError(
            override val code: Int,
            override val reason: String,
        ) : Error()

        data class NetworkError(
            override val code: Int = 500,
            override val reason: String,
        ) : Error()

        data class SerializationError(
            override val code: Int = 500,
            override val reason: String,
        ) : Error()

        data class Unspecified(
            override val code: Int = 500,
            override val reason: String,
        ) : Error()
    }
}

suspend inline fun <I : Any, O : Any> ApiResponse<I>.flatMap(crossinline transform: suspend (value: I) -> ApiResponse<O>): ApiResponse<O> =
    when (this) {
        is ApiResponse.Error -> this
        is ApiResponse.Success -> transform(this.body)
    }

inline fun <I : Any, O : Any> ApiResponse<I>.map(crossinline transform: (value: I) -> O): ApiResponse<O> =
    when (this) {
        is ApiResponse.Error -> this
        is ApiResponse.Success -> ApiResponse.Success(transform(this.body))
    }

suspend inline fun <T : Any> ApiResponse<T>.onSuccess(crossinline block: suspend (value: T) -> Unit): ApiResponse<T> {
    if (this is ApiResponse.Success) {
        block(this.body)
    }
    return this
}

/**
 * Zips two suspend functions that return ApiResponse.
 * If all are successful, it transforms their data.
 * If any fail, it returns the first encountered ApiResponse.Error.
 */
suspend fun <T1, T2, R> zip(
    call1: suspend () -> ApiResponse<T1>,
    call2: suspend () -> ApiResponse<T2>,
    transform: (T1, T2) -> R,
): ApiResponse<R> =
    coroutineScope {
        val deferred1 = async { call1() }
        val deferred2 = async { call2() }

        val response1 = deferred1.await()
        val response2 = deferred2.await()

        if (response1 is ApiResponse.Success && response2 is ApiResponse.Success) {
            ApiResponse.Success(transform(response1.body, response2.body))
        } else {
            listOf(response1, response2)
                .filterIsInstance<ApiResponse.Error>()
                .firstOrNull() ?: ApiResponse.Error.Unspecified(
                999,
                "zip failed but no ApiResponse.Error found",
            )
        }
    }

/**
 * Zips three suspend functions that return ApiResponse.
 * If all are successful, it transforms their data.
 * If any fail, it returns the first encountered ApiResponse.Error.
 */
suspend fun <T1, T2, T3, R> zip(
    call1: suspend () -> ApiResponse<T1>,
    call2: suspend () -> ApiResponse<T2>,
    call3: suspend () -> ApiResponse<T3>,
    transform: (T1, T2, T3) -> R,
): ApiResponse<R> =
    coroutineScope {
        val deferred1 = async { call1() }
        val deferred2 = async { call2() }
        val deferred3 = async { call3() }

        val response1 = deferred1.await()
        val response2 = deferred2.await()
        val response3 = deferred3.await()

        if (response1 is ApiResponse.Success &&
            response2 is ApiResponse.Success &&
            response3 is ApiResponse.Success
        ) {
            ApiResponse.Success(transform(response1.body, response2.body, response3.body))
        } else {
            listOf(response1, response2, response3)
                .filterIsInstance<ApiResponse.Error>()
                .firstOrNull() ?: ApiResponse.Error.Unspecified(
                999,
                "zip failed but no ApiResponse.Error found",
            )
        }
    }

/**
 * Zips four suspend functions that return ApiResponse.
 * If all are successful, it transforms their data.
 * If any fail, it returns the first encountered ApiResponse.Error.
 */
suspend fun <T1, T2, T3, T4, R> zip(
    call1: suspend () -> ApiResponse<T1>,
    call2: suspend () -> ApiResponse<T2>,
    call3: suspend () -> ApiResponse<T3>,
    call4: suspend () -> ApiResponse<T4>,
    transform: (T1, T2, T3, T4) -> R,
): ApiResponse<R> =
    coroutineScope {
        val deferred1 = async { call1() }
        val deferred2 = async { call2() }
        val deferred3 = async { call3() }
        val deferred4 = async { call4() }

        val response1 = deferred1.await()
        val response2 = deferred2.await()
        val response3 = deferred3.await()
        val response4 = deferred4.await()

        if (response1 is ApiResponse.Success &&
            response2 is ApiResponse.Success &&
            response3 is ApiResponse.Success &&
            response4 is ApiResponse.Success
        ) {
            ApiResponse.Success(
                transform(
                    response1.body,
                    response2.body,
                    response3.body,
                    response4.body,
                ),
            )
        } else {
            listOf(response1, response2, response3, response4)
                .filterIsInstance<ApiResponse.Error>()
                .firstOrNull() ?: ApiResponse.Error.Unspecified(
                999,
                "zip failed but no ApiResponse.Error found",
            )
        }
    }
