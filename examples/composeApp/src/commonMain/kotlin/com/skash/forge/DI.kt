package com.skash.forge

import com.russhwolf.settings.Settings
import com.skash.forge.datastore.DataStore
import com.skash.forge.multiplatformsettings.MultiplatformSettingsDataStore
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appContainer = module {
    single<DataStore> { MultiplatformSettingsDataStore(createSettings()) }
    factory<ObserveCounterUseCase> { ObserveCounterUseCase(get()) }
    factory<ExampleUseCase> { ExampleUseCase(get()) }
    viewModelOf(::ExampleViewModel)
}

expect fun createSettings(): Settings
