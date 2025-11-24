package com.skash.forge.network.request

import kotlin.reflect.KClass
import kotlin.reflect.KType

sealed class ApiRequest {
    abstract val route: Route
    abstract val headers: Map<String, String>
    abstract val parameters: Map<String, String>

    sealed class NoBodyRequest : ApiRequest()

    data class Get(
        override val route: Route,
        override val headers: Map<String, String>,
        override val parameters: Map<String, String>,
    ) : NoBodyRequest()

    data class Delete(
        override val route: Route,
        override val headers: Map<String, String>,
        override val parameters: Map<String, String>,
    ) : NoBodyRequest()

    sealed class BodyRequest : ApiRequest() {
        abstract val body: Any
        abstract val bodyType: KType
        abstract val bodyClass: KClass<*>
    }

    data class Post(
        override val route: Route,
        override val headers: Map<String, String>,
        override val parameters: Map<String, String>,
        override val body: Any,
        override val bodyType: KType,
        override val bodyClass: KClass<*>,
    ) : BodyRequest()

    data class Put(
        override val route: Route,
        override val headers: Map<String, String>,
        override val parameters: Map<String, String>,
        override val body: Any,
        override val bodyType: KType,
        override val bodyClass: KClass<*>,
    ) : BodyRequest()

    data class Patch(
        override val route: Route,
        override val headers: Map<String, String>,
        override val parameters: Map<String, String>,
        override val body: Any,
        override val bodyType: KType,
        override val bodyClass: KClass<*>,
    ) : BodyRequest()
}