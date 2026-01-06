package com.skash.forge.network.ktor

import com.skash.forge.logger.i
import com.skash.forge.network.client.HttpClient
import com.skash.forge.network.client.HttpClientBundle
import com.skash.forge.network.client.ApiRequestBuilder
import com.skash.forge.network.client.StateClearable
import com.skash.forge.network.logging.HttpLogLevel
import com.skash.forge.network.request.ApiRequest
import com.skash.forge.network.request.formdata.MultipartPart
import com.skash.forge.network.request.formdata.MultipartPayload
import com.skash.forge.network.response.ApiResponse
import com.skash.forge.network.response.RawResponse
import com.skash.forge.network.session.TokenAuthenticator
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.reflect.KClass
import kotlin.reflect.KType

import io.ktor.client.HttpClient as KtorHttpClient

class KtorApiClient internal constructor(
    private val httpClient: KtorHttpClient,
    private val defaultHeaders: HeadersBuilder.() -> Unit,
) : HttpClient {
    override suspend fun <Input : Any, Output : Any> execute(
        dtoClass: KClass<Input>,
        dtoType: KType,
        mapper: (Input) -> Output,
        requestBuilder: ApiRequestBuilder.() -> Unit,
    ): ApiResponse<Output> =
        executeRequest(requestBuilder) { response ->
            val body: Input = response.body(TypeInfo(type = dtoClass, kotlinType = dtoType))
            ApiResponse.Success(mapper(body), response.status.value)
        }

    override suspend fun executeRaw(requestBuilder: ApiRequestBuilder.() -> Unit): ApiResponse<RawResponse> =
        executeRequest(requestBuilder) { response ->
            val rawResponse =
                RawResponse(
                    status = response.status.value,
                    headers = response.headers.entries().associate { it.key to it.value },
                    body = response.bodyAsText(),
                )
            ApiResponse.Success(rawResponse, response.status.value)
        }

    private suspend fun <T> executeRequest(
        requestBuilder: ApiRequestBuilder.() -> Unit,
        successBlock: suspend (HttpResponse) -> ApiResponse.Success<T>,
    ): ApiResponse<T> {
        val apiRequest = ApiRequestBuilder().apply(requestBuilder).build()

        return try {
            val response = httpClient.request { buildKtorRequest(apiRequest) }

            if (!response.status.isSuccess()) {
                ApiResponse.Error.HttpError(
                    code = response.status.value,
                    reason = response.bodyAsText(),
                )
            } else {
                successBlock(response)
            }
        } catch (e: ClientRequestException) {
            ApiResponse.Error.HttpError(e.response.status.value, e.message)
        } catch (e: ServerResponseException) {
            ApiResponse.Error.HttpError(e.response.status.value, e.message)
        } catch (e: SerializationException) {
            ApiResponse.Error.SerializationError(reason = e.message.orEmpty())
        } catch (e: IOException) {
            ApiResponse.Error.NetworkError(reason = e.message.orEmpty())
        } catch (e: Exception) {
            ApiResponse.Error.Unspecified(reason = e.message.orEmpty())
        }
    }

    private fun HttpRequestBuilder.buildKtorRequest(request: ApiRequest) {
        url {
            takeFrom(request.route.path)
            request.parameters.forEach { (key, value) ->
                parameters.append(key, value)
            }
        }

        headers {
            defaultHeaders(this)
        }

        request.headers.forEach { (key, value) ->
            this.headers[key] = value
        }

        when (request) {
            is ApiRequest.Get -> {
                method = HttpMethod.Get
            }

            is ApiRequest.Delete -> {
                method = HttpMethod.Delete
            }

            is ApiRequest.BodyRequest -> {
                when (request.body is MultipartPayload) {
                    true -> {
                        val payload = request.body as MultipartPayload
                        setBody(createMultipartFormDataContent(payload.parts))
                    }

                    false -> {
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                        val typeInfo = TypeInfo(request.bodyClass, request.bodyType)
                        setBody(request.body, typeInfo)
                    }
                }

                method =
                    when (request) {
                        is ApiRequest.Post -> HttpMethod.Post
                        is ApiRequest.Put -> HttpMethod.Put
                        is ApiRequest.Patch -> HttpMethod.Patch
                    }
            }
        }
    }

    private fun createMultipartFormDataContent(parts: List<MultipartPart>): MultiPartFormDataContent =
        MultiPartFormDataContent(
            formData {
                parts.forEach { part ->
                    when (part) {
                        is MultipartPart.FormPart -> {
                            append(part.name, part.value)
                        }

                        is MultipartPart.FilePart -> {
                            append(
                                key = part.name,
                                value = part.content,
                                headers =
                                    Headers.build {
                                        append(HttpHeaders.ContentType, part.mimeType)
                                        append(
                                            HttpHeaders.ContentDisposition,
                                            "filename=\"${part.filename}\""
                                        )
                                    },
                            )
                        }
                    }
                }
            },
        )

    companion object {
        operator fun invoke(block: KtorApiClientConfig.() -> Unit = {}): HttpClientBundle {
            val config = KtorApiClientConfig().apply(block)

            val defaultHttpClientConfig: HttpClientConfig<*>.() -> Unit = {
                install(ContentNegotiation) {
                    json(config.json)
                }
                install(Logging) {
                    level = config.logLevel.toKtorLogLevel()
                    logger =
                        object : Logger {
                            override fun log(message: String) {
                                config.logger.i("Ktor") { message }
                            }
                        }
                }
            }

            val authTaskClient: HttpClient by lazy {
                val httpClient =
                    KtorHttpClient(getHttpClientEngineFactory()) {
                        apply(defaultHttpClientConfig)
                    }
                KtorApiClient(httpClient, config.defaultHeaders)
            }

            val mainHttpClient =
                KtorHttpClient(getHttpClientEngineFactory()) {
                    apply(defaultHttpClientConfig)

                    config.tokenAuthenticator?.let { authenticator ->
                        install(Auth) {
                            bearer {
                                loadTokens {
                                    authenticator.loadTokens(authTaskClient)?.let {
                                        BearerTokens(it.bearer, it.refresh)
                                    }
                                }
                                refreshTokens {
                                    authenticator.refreshTokens(authTaskClient)?.let {
                                        BearerTokens(it.bearer, it.refresh)
                                    }
                                }
                            }
                        }
                    }
                }

            val apiClient = KtorApiClient(mainHttpClient, config.defaultHeaders)

            val stateClearable =
                KtorClientStateClearer(
                    mainHttpClient,
                    config.tokenAuthenticator,
                )

            return HttpClientBundle(
                client = apiClient,
                stateClearable = stateClearable,
            )
        }
    }
}

internal class KtorClientStateClearer(
    private val httpClient: KtorHttpClient,
    private val tokenAuthenticator: TokenAuthenticator?,
) : StateClearable {
    override suspend fun clearState() {
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        tokenAuthenticator?.clearToken()
    }
}

fun HttpLogLevel.toKtorLogLevel() =
    when (this) {
        HttpLogLevel.All -> LogLevel.ALL
        HttpLogLevel.Headers -> LogLevel.HEADERS
        HttpLogLevel.Body -> LogLevel.BODY
        HttpLogLevel.Info -> LogLevel.INFO
        HttpLogLevel.None -> LogLevel.NONE
    }