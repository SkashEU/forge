package com.skash.forge

import com.russhwolf.settings.Settings

actual fun createSettings(): Settings {
    return StorageSettings()
}