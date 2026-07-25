package com.hermes.companion.core.di

import com.hermes.companion.ui.screenmodel.HomeViewModel
import com.hermes.companion.ui.screenmodel.MissionViewModel
import com.hermes.companion.ui.screenmodel.AgentViewModel
import com.hermes.companion.ui.screenmodel.MemoryViewModel
import com.hermes.companion.ui.screenmodel.LogsViewModel
import com.hermes.companion.ui.screenmodel.BrowserViewModel
import com.hermes.companion.ui.screenmodel.AndroidControlViewModel
import com.hermes.companion.ui.screenmodel.PerformanceViewModel
import com.hermes.companion.ui.screenmodel.PluginsViewModel
import com.hermes.companion.ui.screenmodel.SettingsViewModel
import com.hermes.companion.ui.screenmodel.ToolsViewModel
import com.hermes.companion.ui.screenmodel.DeveloperViewModel
import com.hermes.companion.ui.screenmodel.VoiceViewModel
import com.hermes.companion.ui.screenmodel.FloatingOverlayViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

/**
 * ViewModel DI module.
 * All ViewModels registered with Koin's viewModel() for proper lifecycle.
 * Dependencies (Repositories) are provided by repositoryModule.
 */
val appModule = module {
    // ViewModels - use viewModel() for proper ViewModelStore lifecycle
    // Koin will auto-inject constructor dependencies from repositoryModule
    viewModel { HomeViewModel() }
    viewModel { MissionViewModel(get()) }
    viewModel { AgentViewModel(get()) }
    viewModel { MemoryViewModel(get()) }
    viewModel { LogsViewModel(get()) }
    viewModel { BrowserViewModel() }
    viewModel { AndroidControlViewModel() }
    viewModel { PerformanceViewModel() }
    viewModel { PluginsViewModel() }
    viewModel { SettingsViewModel() }
    viewModel { ToolsViewModel() }
    viewModel { DeveloperViewModel() }
    viewModel { VoiceViewModel() }
    viewModel { FloatingOverlayViewModel() }
}