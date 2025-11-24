package com.skash.forge.network.client

import com.skash.forge.network.request.ApiRequest
import com.skash.forge.network.request.HttpHeader
import com.skash.forge.network.request.HttpMethod
import com.skash.forge.network.request.Route
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class ApiRequestBuilder {

    private var httpMethod: HttpMethod? = null
    private var route: Route? = null
    private val headers = mutableMapOf<String, String>()
    private val parameters = mutableMapOf<String, String>()
    @PublishedApi
    internal var requestBody: RequestBody? = null

    fun get(url: String) = setRequestLine(HttpMethod.Get, Route(basePath = url))
    fun post(url: String) = setRequestLine(HttpMethod.Post, Route(basePath = url))
    fun put(url: String) = setRequestLine(HttpMethod.Put, Route(basePath = url))
    fun patch(url: String) = setRequestLine(HttpMethod.Patch, Route(basePath = url))
    fun delete(url: String) = setRequestLine(HttpMethod.Delete, Route(basePath = url))

    fun get(route: Route) = setRequestLine(HttpMethod.Get, route)
    fun post(route: Route) = setRequestLine(HttpMethod.Post, route)
    fun put(route: Route) = setRequestLine(HttpMethod.Put, route)
    fun patch(route: Route) = setRequestLine(HttpMethod.Patch, route)
    fun delete(route: Route) = setRequestLine(HttpMethod.Delete, route)

    inline fun <reified T: Any> body(body: T) {
        this.requestBody = RequestBody(
            instance = body,
            kClass = T::class,
            kType = typeOf<T>()
        )
    }

    fun header(key: String, value: String) {
        headers[key] = value
    }

    fun header(header: HttpHeader) {
        headers[header.key] = header.value
    }

    fun headers(block: MutableMap<String, String>.() -> Unit) {
        headers.apply(block)
    }

    fun parameters(block: MutableMap<String, String>.() -> Unit) {
        parameters.apply(block)
    }

    fun build(): ApiRequest {
        val finalMethod = httpMethod
        val finalRoute = route

        if (finalMethod == null || finalRoute == null) {
            throw IllegalStateException("HTTP method and URL/Endpoint must be set. (e.g., call `get(\\\"/users\\\")`)\"")
        }

        val finalHeaders = headers.toMap()
        val finalParameters = parameters.toMap()

        return when(finalMethod) {
            HttpMethod.Get -> ApiRequest.Get(finalRoute, finalHeaders, finalParameters)
            HttpMethod.Delete -> ApiRequest.Delete(finalRoute, finalHeaders, finalParameters)
            HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch -> {
                val bodyData = requestBody
                    ?: throw IllegalStateException("Body must be set for a $finalMethod request.")

                when (finalMethod) {
                    HttpMethod.Post -> ApiRequest.Post(
                        route = finalRoute,
                        headers = finalHeaders,
                        parameters = finalParameters,
                        body = bodyData.instance,
                        bodyType = bodyData.kType,
                        bodyClass = bodyData.kClass
                    )
                    HttpMethod.Put -> ApiRequest.Put(
                        route = finalRoute,
                        headers = finalHeaders,
                        parameters = finalParameters,
                        body = bodyData.instance,
                        bodyType = bodyData.kType,
                        bodyClass = bodyData.kClass
                    )
                    HttpMethod.Patch -> ApiRequest.Patch(
                        route = finalRoute,
                        headers = finalHeaders,
                        parameters = finalParameters,
                        body = bodyData.instance,
                        bodyType = bodyData.kType,
                        bodyClass = bodyData.kClass
                    )
                    else -> error("Impossible HTTP method state.")
                }
            }
        }
    }

    private fun setRequestLine(method: HttpMethod, route: Route) {
        this.httpMethod = method
        this.route = route
    }

    @PublishedApi
    internal data class RequestBody(
        val instance: Any,
        val kClass: KClass<*>,
        val kType: KType
    )
}

