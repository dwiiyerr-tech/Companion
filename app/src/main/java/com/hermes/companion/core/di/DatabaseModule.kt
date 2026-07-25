package com.hermes.companion.core.di

import org.koin.dsl.module

/**
 * Room Database module.
 * Currently provides databaseModule as a no-op placeholder.
 * When Room entities are added, register AppDatabase here.
 */
val databaseModule = module {
    // Room database — register when entities exist:
    // single { AppDatabase.getInstance(get()) }
    // single { get<AppDatabase>().agentDao() }
    // single { get<AppDatabase>().missionDao() }
    // single { get<AppDatabase>().memoryDao() }
}
