package com.skash.forge.network.client

import com.skash.forge.network.request.formdata.MultipartPart
import com.skash.forge.network.request.formdata.MultipartPayload

class MultipartBodyBuilder {
    private val parts = mutableListOf<MultipartPart>()

    fun add(
        name: String,
        value: String,
        contentType: String? = null,
    ) {
        parts.add(MultipartPart.FormPart(name, value, contentType))
    }

    fun addFile(
        name: String,
        filename: String,
        content: ByteArray,
        contentType: String,
    ) {
        parts.add(MultipartPart.FilePart(name, filename, content, contentType))
    }

    internal fun build(): MultipartPayload = MultipartPayload(parts.toList())
}
