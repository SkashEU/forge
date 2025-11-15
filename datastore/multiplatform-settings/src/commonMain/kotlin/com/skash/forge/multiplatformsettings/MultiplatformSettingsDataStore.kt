@file:OptIn(ExperimentalSettingsApi::class)

package com.skash.forge.multiplatformsettings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import com.skash.forge.datastore.DataEntry
import com.skash.forge.datastore.DataStore
import com.skash.forge.datastore.PrimitiveType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json

class MultiplatformSettingsDataStore(
    private val settings: FlowSettings = createFlowSettings(),
    private val defaultJson: Json = Json
) : DataStore {

    override fun <T> observe(dataEntry: DataEntry<T>): Flow<T> {
        val key = dataEntry.key
        val defaultValue = dataEntry.defaultValue

        dataEntry.serializer?.let { serializer ->
            val json = dataEntry.json ?: defaultJson
            val defaultJsonString = json.encodeToString(serializer, defaultValue)

            return settings.getStringFlow(
                key = key,
                defaultValue = defaultJsonString
            ).map { json.decodeFromString(serializer, defaultJsonString) }
        }

        return when (dataEntry.primitiveType) {
            PrimitiveType.INT -> settings.getIntFlow(key, defaultValue as Int) as Flow<T>
            PrimitiveType.STRING -> settings.getStringFlow(key, defaultValue as String) as Flow<T>
            PrimitiveType.BOOLEAN -> settings.getBooleanFlow(key, defaultValue as Boolean) as Flow<T>
            PrimitiveType.LONG -> settings.getLongFlow(key, defaultValue as Long) as Flow<T>
            PrimitiveType.FLOAT -> settings.getFloatFlow(key, defaultValue as Float) as Flow<T>
            PrimitiveType.DOUBLE -> settings.getDoubleFlow(key, defaultValue as Double) as Flow<T>
            null -> throw IllegalArgumentException("Unknown DataEntry type for key $key")
        }.onEach {
            println("On Thread:")
        }
    }

    override suspend fun <T> get(dataEntry: DataEntry<T>): T? {
        val key = dataEntry.key

        dataEntry.serializer?.let { serializer ->
            val json = dataEntry.json ?: defaultJson
            val jsonString = settings.getStringOrNull(key)
            return jsonString?.let { json.decodeFromString(serializer, it) }
        }

        return when (dataEntry.primitiveType) {
            PrimitiveType.INT -> settings.getIntOrNull(key) as T?
            PrimitiveType.STRING -> settings.getStringOrNull(key) as T?
            PrimitiveType.BOOLEAN -> settings.getBooleanOrNull(key) as T?
            PrimitiveType.LONG -> settings.getLongOrNull(key) as T?
            PrimitiveType.FLOAT -> settings.getFloatOrNull(key) as T?
            PrimitiveType.DOUBLE -> settings.getDoubleOrNull(key) as T?
            null -> throw IllegalArgumentException("Unknown DataEntry type for key $key")
        }
    }

    override suspend fun <T> set(dataEntry: DataEntry<T>, value: T) {
        val key = dataEntry.key

        dataEntry.serializer?.let { serializer ->
            val json = dataEntry.json ?: defaultJson
            val jsonString = json.encodeToString(serializer, value)
            settings.putString(key, jsonString)
            return
        }

        when (dataEntry.primitiveType) {
            PrimitiveType.INT -> settings.putInt(key, value as Int)
            PrimitiveType.STRING -> settings.putString(key, value as String)
            PrimitiveType.BOOLEAN -> settings.putBoolean(key, value as Boolean)
            PrimitiveType.LONG -> settings.putLong(key, value as Long)
            PrimitiveType.FLOAT -> settings.putFloat(key, value as Float)
            PrimitiveType.DOUBLE -> settings.putDouble(key, value as Double)
            null -> throw IllegalArgumentException("Unknown DataEntry type for key $key")
        }
    }

    override suspend fun <T> delete(dataEntry: DataEntry<T>) {
        settings.remove(dataEntry.key)
    }
}
