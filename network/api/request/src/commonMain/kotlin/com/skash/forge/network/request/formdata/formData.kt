package com.skash.forge.network.request.formdata

sealed class MultipartPart {
    abstract val name: String
    abstract val mimeType: String?

    data class FormPart(
        override val name: String,
        val value: String,
        override val mimeType: String? = null,
    ) : MultipartPart()

    data class FilePart(
        override val name: String,
        val filename: String,
        val content: ByteArray,
        override val mimeType: String,
    ) : MultipartPart() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as FilePart

            if (name != other.name) return false
            if (filename != other.filename) return false
            if (!content.contentEquals(other.content)) return false
            if (mimeType != other.mimeType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + filename.hashCode()
            result = 31 * result + content.contentHashCode()
            result = 31 * result + mimeType.hashCode()
            return result
        }
    }
}

data class MultipartPayload(
    val parts: List<MultipartPart>,
)
