@file:OptIn(ExperimentalForeignApi::class)

package com.skash.forge.multiplatformsettings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(markerClass = [com.russhwolf.settings.ExperimentalSettingsApi::class])
internal actual fun createFlowSettings(): FlowSettings {
    val dataStore = PreferenceDataStoreFactory.createWithPath {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val pathString = requireNotNull(documentDirectory).path + "/$PREF_NAME"
        pathString.toPath()
    }
    return DataStoreSettings(dataStore)
}