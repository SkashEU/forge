package com.skash.forge.multiplatformsettings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.observable.makeObservable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@OptIn(markerClass = [ExperimentalSettingsApi::class])
internal actual fun createFlowSettings(): FlowSettings {
    val settings = StorageSettings().makeObservable()
    return FlowSettingsWrapper(settings)
}

@OptIn(ExperimentalSettingsApi::class)
internal class FlowSettingsWrapper(
    private val settings: ObservableSettings
): FlowSettings {

    override suspend fun keys(): Set<String> = settings.keys

    override suspend fun size(): Int = settings.size

    override suspend fun clear() {
        settings.clear()
    }

    override suspend fun remove(key: String) {
        settings.remove(key)
    }

    override suspend fun hasKey(key: String): Boolean = settings.hasKey(key)

    override suspend fun putInt(key: String, value: Int) {
        settings.putInt(key, value)
    }

    override suspend fun putLong(key: String, value: Long) {
        settings.putLong(key, value)
    }

    override suspend fun putString(key: String, value: String) {
        settings.putString(key, value)
    }

    override suspend fun putFloat(key: String, value: Float) {
        settings.putFloat(key, value)
    }

    override suspend fun putDouble(key: String, value: Double) {
        settings.putDouble(key, value)
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        settings.putBoolean(key, value)
    }

    override fun getIntFlow(key: String, defaultValue: Int): Flow<Int> = callbackFlow {
        trySend(settings.getInt(key, defaultValue))

        val listener = settings.addIntListener(key, defaultValue) { newValue ->
            trySend(newValue)
        }

        awaitClose { listener.deactivate() }
    }

    override fun getIntOrNullFlow(key: String): Flow<Int?> = callbackFlow {
        trySend(settings.getIntOrNull(key))
        val listener = settings.addIntOrNullListener(key) { newValue ->
            trySend(newValue)
        }
        awaitClose { listener.deactivate() }
    }

    override fun getLongFlow(key: String, defaultValue: Long): Flow<Long> = callbackFlow {
        trySend(settings.getLong(key, defaultValue))
        val listener = settings.addLongListener(key, defaultValue) { newValue ->
            trySend(newValue)
        }
        awaitClose { listener.deactivate() }
    }

    override fun getLongOrNullFlow(key: String): Flow<Long?> = callbackFlow {
        trySend(settings.getLongOrNull(key))
        val listener = settings.addLongOrNullListener(key) { newValue ->
            trySend(newValue)
        }
        awaitClose { listener.deactivate() }
    }

    override fun getStringFlow(key: String, defaultValue: String): Flow<String> = callbackFlow {
        trySend(settings.getString(key, defaultValue))
        val listener = settings.addStringListener(key, defaultValue) { newValue ->
            trySend(newValue)
        }
        awaitClose { listener.deactivate() }
    }

    override fun getStringOrNullFlow(key: String): Flow<String?> = callbackFlow {
        trySend(settings.getStringOrNull(key))
        val listener = settings.addStringOrNullListener(key) { newValue ->
            trySend(newValue)
        }
        awaitClose { listener.deactivate() }
    }

    override fun getFloatFlow(key: String, defaultValue: Float): Flow<Float> = callbackFlow {
        trySend(settings.getFloat(key, defaultValue))
        val listener = settings.addFloatListener(key, defaultValue) { newValue ->
            trySend(newValue)
        }
        awaitClose { listener.deactivate() }
    }

    override fun getFloatOrNullFlow(key: String): Flow<Float?> = callbackFlow {
        trySend(settings.getFloatOrNull(key))
        val listener = settings.addFloatOrNullListener(key) { newValue ->
            trySend(newValue)
        }
        awaitClose { listener.deactivate() }
    }

    override fun getDoubleFlow(key: String, defaultValue: Double): Flow<Double> = callbackFlow {
        trySend(settings.getDouble(key, defaultValue))
        val listener = settings.addDoubleListener(key, defaultValue) { newValue ->
            trySend(newValue)
        }
        awaitClose { listener.deactivate() }
    }

    override fun getDoubleOrNullFlow(key: String): Flow<Double?> = callbackFlow {
        trySend(settings.getDoubleOrNull(key))
        val listener = settings.addDoubleOrNullListener(key) { newValue ->
            trySend(newValue)
        }
        awaitClose { listener.deactivate() }
    }

    override fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> = callbackFlow {
        trySend(settings.getBoolean(key, defaultValue))
        val listener = settings.addBooleanListener(key, defaultValue) { newValue ->
            trySend(newValue)
        }
        awaitClose { listener.deactivate() }
    }

    override fun getBooleanOrNullFlow(key: String): Flow<Boolean?> = callbackFlow {
        trySend(settings.getBooleanOrNull(key))
        val listener = settings.addBooleanOrNullListener(key) { newValue ->
            trySend(newValue)
        }
        awaitClose { listener.deactivate() }
    }
}