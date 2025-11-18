package com.skash.forge

import com.russhwolf.settings.ExperimentalSettingsApi
import com.skash.forge.data.repository.DummyRepositoryImpl
import com.skash.forge.datastore.DataStore
import com.skash.forge.datastore.multiplatformsettings.MultiplatformSettingsDataStore
import com.skash.forge.domain.usecase.DecreaseCounterUseCase
import com.skash.forge.domain.usecase.IncreaseCounterUseCase
import com.skash.forge.domain.usecase.ObserveCounterUseCase
import com.skash.forge.domain.repository.DummyRepository
import com.skash.forge.domain.usecase.GetDummyPostsUseCase
import com.skash.forge.event.DefaultEventBus
import com.skash.forge.event.EventBus
import com.skash.forge.feature.detail.DetailViewModel
import com.skash.forge.feature.main.MainViewModel
import com.skash.forge.navigation.NavigationDispatcher
import com.skash.forge.navigation.nav2.DefaultNavigationDispatcher
import com.skash.forge.network.client.HttpClient
import com.skash.forge.network.ktor.KtorApiClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
val appContainer = module {
    single<DataStore> { MultiplatformSettingsDataStore() }
    factory<ObserveCounterUseCase> { ObserveCounterUseCase(get()) }
    factory<IncreaseCounterUseCase> { IncreaseCounterUseCase(get()) }
    factory<DecreaseCounterUseCase> { DecreaseCounterUseCase(get()) }
    factory<GetDummyPostsUseCase> { GetDummyPostsUseCase(get()) }

    single<HttpClient> { KtorApiClient {}.client }
    single<DummyRepository> { DummyRepositoryImpl(get()) }

    viewModelOf(::MainViewModel)
    viewModelOf(::DetailViewModel)
    single<NavigationDispatcher> { DefaultNavigationDispatcher() }
    single<EventBus<UIEvent>> { DefaultEventBus() }
}

