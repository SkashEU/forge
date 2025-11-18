package com.skash.forge.datastore.multiplatformsettings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import okio.Path.Companion.toPath
import java.io.File

@OptIn(markerClass = [ExperimentalSettingsApi::class])
internal actual fun createFlowSettings(): FlowSettings {
    val dataStore = PreferenceDataStoreFactory.createWithPath {
        val file = File(System.getProperty("java.io.tmpdir"), PREF_NAME)
        file.absolutePath.toPath()
    }

    return DataStoreSettings(dataStore)
}