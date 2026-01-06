package com.skash.forge.network.request

open class Route(
    private val parent: Route? = null,
    private val segment: String,
) : Endpoint {
    private var rootBasePath: String? = null

    private val root: Route by lazy {
        parent?.root ?: this
    }

    constructor(basePath: String, segment: String = "") : this(
        parent = null,
        segment = segment,
    ) {
        this.rootBasePath = basePath
    }

    private val relativePath: String by lazy {
        parent?.relativePath?.let { "$it/$segment" } ?: segment
    }

    override val path: String by lazy {
        val base = root.rootBasePath.orEmpty().trimEnd('/')
        val relative = relativePath.trimStart('/')

        return@lazy if (base.isEmpty()) relative else "$base/$relative"
    }
}