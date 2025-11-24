package com.skash.forge.datastore

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

enum class PrimitiveType {
    INT, STRING, BOOLEAN, LONG, FLOAT, DOUBLE
}

abstract class DataEntry<T> internal constructor(
    val key: String,
    val defaultValue: T
) {
    abstract val primitiveType: PrimitiveType?

    abstract val serializer: KSerializer<T>?

    open val json: Json? = null

    companion object {
        fun int(key: String, defaultValue: Int): DataEntry<Int> =
            IntEntry(key, defaultValue)

        fun string(key: String, defaultValue: String): DataEntry<String> =
            StringEntry(key, defaultValue)

        fun boolean(key: String, defaultValue: Boolean): DataEntry<Boolean> =
            BooleanEntry(key, defaultValue)

        fun long(key: String, defaultValue: Long): DataEntry<Long> =
            LongEntry(key, defaultValue)

        fun float(key: String, defaultValue: Float): DataEntry<Float> =
            FloatEntry(key, defaultValue)

        fun double(key: String, defaultValue: Double): DataEntry<Double> =
            DoubleEntry(key, defaultValue)

        fun <T> serializable(
            key: String,
            defaultValue: T,
            serializer: KSerializer<T>,
            json: Json
        ): DataEntry<T> = SerializableEntry(key, defaultValue, serializer, json)
    }
}

private class IntEntry(key: String, d: Int) : DataEntry<Int>(key, d) {
    override val primitiveType = PrimitiveType.INT
    override val serializer = null
}

private class StringEntry(key: String, d: String) : DataEntry<String>(key, d) {
    override val primitiveType = PrimitiveType.STRING
    override val serializer = null
}

private class BooleanEntry(key: String, d: Boolean) : DataEntry<Boolean>(key, d) {
    override val primitiveType = PrimitiveType.BOOLEAN
    override val serializer = null
}

private class LongEntry(key: String, d: Long) : DataEntry<Long>(key, d) {
    override val primitiveType = PrimitiveType.LONG
    override val serializer = null
}

private class FloatEntry(key: String, d: Float) : DataEntry<Float>(key, d) {
    override val primitiveType = PrimitiveType.FLOAT
    override val serializer = null
}

private class DoubleEntry(key: String, d: Double) : DataEntry<Double>(key, d) {
    override val primitiveType = PrimitiveType.DOUBLE
    override val serializer = null
}

private class SerializableEntry<T>(
    key: String,
    defaultValue: T,
    override val serializer: KSerializer<T>,
    override val json: Json
) : DataEntry<T>(key, defaultValue) {
    override val primitiveType = null
}
