@file:OptIn(ExperimentalSettingsImplementation::class)

package com.skash.forge.multiplatformsettings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.startup.Initializer
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings

internal object DataStoreHolder {
    lateinit var dataStore: DataStore<Preferences>
        internal set

    fun isInitialized(): Boolean = ::dataStore.isInitialized
}

class DataStoreInitializer : Initializer<DataStore<Preferences>> {
    override fun create(context: Context): DataStore<Preferences> {
        val dataStore = PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("${context.packageName}_preferences")
        }
        DataStoreHolder.dataStore = dataStore

        return dataStore
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> = emptyList()
}

@OptIn(markerClass = [ExperimentalSettingsApi::class])
internal actual fun createFlowSettings(): FlowSettings {
    if (!DataStoreHolder.isInitialized()) {
        throw IllegalStateException("DataStore must be initialized")
    }

    val dataStore = DataStoreHolder.dataStore
    return DataStoreSettings(dataStore)
}