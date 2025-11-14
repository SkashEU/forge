package com.skash.forge.multiplatformsettings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.*
import com.russhwolf.settings.observable.makeObservable
import com.skash.forge.datastore.DataEntry
import com.skash.forge.datastore.DataStore
import com.skash.forge.datastore.PrimitiveType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class MultiplatformSettingsDataStore(
    settings: Settings,
    private val defaultJson: Json = Json
) : DataStore {

    @OptIn(ExperimentalSettingsApi::class)
    private val observableSettings = settings.makeObservable()

    @OptIn(ExperimentalSettingsApi::class)
    override fun <T> observe(dataEntry: DataEntry<T>): Flow<T> {
        val key = dataEntry.key
        val defaultValue = dataEntry.defaultValue

        dataEntry.serializer?.let { serializer ->
            val json = dataEntry.json ?: defaultJson
            val defaultJsonString = json.encodeToString(serializer, defaultValue)

            return observableSettings.getStringFlow(
                key = key,
                defaultValue = defaultJsonString
            ).map { json.decodeFromString(serializer, defaultJsonString) }
        }

        return when (dataEntry.primitiveType) {
            PrimitiveType.INT -> observableSettings.getIntFlow(key, defaultValue as Int) as Flow<T>
            PrimitiveType.STRING -> observableSettings.getStringFlow(key, defaultValue as String) as Flow<T>
            PrimitiveType.BOOLEAN -> observableSettings.getBooleanFlow(key, defaultValue as Boolean) as Flow<T>
            PrimitiveType.LONG -> observableSettings.getLongFlow(key, defaultValue as Long) as Flow<T>
            PrimitiveType.FLOAT -> observableSettings.getFloatFlow(key, defaultValue as Float) as Flow<T>
            PrimitiveType.DOUBLE -> observableSettings.getDoubleFlow(key, defaultValue as Double) as Flow<T>
            null -> throw IllegalArgumentException("Unknown DataEntry type for key $key")
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun <T> get(dataEntry: DataEntry<T>): T? {
        val key = dataEntry.key

        dataEntry.serializer?.let { serializer ->
            val json = dataEntry.json ?: defaultJson
            val jsonString = observableSettings.getStringOrNull(key)
            return jsonString?.let { json.decodeFromString(serializer, it) }
        }

        return when (dataEntry.primitiveType) {
            PrimitiveType.INT -> observableSettings.getIntOrNull(key) as T?
            PrimitiveType.STRING -> observableSettings.getStringOrNull(key) as T?
            PrimitiveType.BOOLEAN -> observableSettings.getBooleanOrNull(key) as T?
            PrimitiveType.LONG -> observableSettings.getLongOrNull(key) as T?
            PrimitiveType.FLOAT -> observableSettings.getFloatOrNull(key) as T?
            PrimitiveType.DOUBLE -> observableSettings.getDoubleOrNull(key) as T?
            null -> throw IllegalArgumentException("Unknown DataEntry type for key $key")
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun <T> set(dataEntry: DataEntry<T>, value: T) {
        val key = dataEntry.key

        dataEntry.serializer?.let { serializer ->
            val json = dataEntry.json ?: defaultJson
            val jsonString = json.encodeToString(serializer, value)
            observableSettings.putString(key, jsonString)
            return
        }

        when (dataEntry.primitiveType) {
            PrimitiveType.INT -> observableSettings.putInt(key, value as Int)
            PrimitiveType.STRING -> observableSettings.putString(key, value as String)
            PrimitiveType.BOOLEAN -> observableSettings.putBoolean(key, value as Boolean)
            PrimitiveType.LONG -> observableSettings.putLong(key, value as Long)
            PrimitiveType.FLOAT -> observableSettings.putFloat(key, value as Float)
            PrimitiveType.DOUBLE -> observableSettings.putDouble(key, value as Double)
            null -> throw IllegalArgumentException("Unknown DataEntry type for key $key")
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun <T> delete(dataEntry: DataEntry<T>) {
        observableSettings.remove(dataEntry.key)
    }
}
