package com.skash.forge

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.skash.forge.datastore.DataStore
import com.skash.forge.multiplatformsettings.MultiplatformSettingsDataStore
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
val appContainer = module {
    single<DataStore> { MultiplatformSettingsDataStore() }
    factory<ObserveCounterUseCase> { ObserveCounterUseCase(get()) }
    factory<ExampleUseCase> { ExampleUseCase(get()) }
    viewModelOf(::ExampleViewModel)
}

