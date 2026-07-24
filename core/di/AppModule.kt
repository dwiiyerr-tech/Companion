package com.hermes.companion.core.di

import com.hermes.companion.core.event.EventBus
import com.hermes.companion.core.network.HermesWebSocketClient
import org.koin.dsl.module

val appModule = module {

    // ── Network ────────────────────────────────────────────────
    single { HermesWebSocketClient() }

    // ── EventBus ───────────────────────────────────────────────
    single { EventBus() }

    // ViewModels will be added when UI screens are built:
    // viewModel { DashboardViewModel(get(), get(), get()) }
    // viewModel { MissionDetailViewModel(get(), get()) }
    // viewModel { SettingsViewModel(get()) }
}