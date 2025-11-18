package com.skash.forge.datastore.multiplatformsettings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings

internal const val PREF_NAME = "datastore.preferences_pb"

@OptIn(markerClass = [ExperimentalSettingsApi::class])
internal expect fun createFlowSettings(): FlowSettings