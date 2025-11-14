package com.skash.forge

import android.content.Context
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

@OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)
actual fun createSettings(): Settings {

    return SharedPreferencesSettings(
        PlatformContext.getContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
    )
}

internal object PlatformContext {
    private var appContext: Context? = null

    fun init(context: Context) {
        this.appContext = context.applicationContext
    }

    internal fun getContext(): Context {
        return appContext
            ?: throw IllegalStateException("PlatformContext.init(context) must be called first in your Application class.")
    }
}