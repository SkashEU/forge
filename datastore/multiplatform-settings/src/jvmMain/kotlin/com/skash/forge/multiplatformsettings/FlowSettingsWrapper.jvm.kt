package com.skash.forge.multiplatformsettings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.russhwolf.settings.datastore.DataStoreSettings
import okio.Path.Companion.toPath
import java.io.File

@OptIn(markerClass = [com.russhwolf.settings.ExperimentalSettingsApi::class])
internal actual fun createFlowSettings(): com.russhwolf.settings.coroutines.FlowSettings {
    val dataStore = PreferenceDataStoreFactory.createWithPath {
        val file = File(System.getProperty("java.io.tmpdir"), PREF_NAME)
        file.absolutePath.toPath()
    }

    return DataStoreSettings(dataStore)
}