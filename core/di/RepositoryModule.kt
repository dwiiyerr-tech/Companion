package com.hermes.companion.core.di

import org.koin.dsl.module

/**
 * Repository bindings.
 * Add concrete repository implementations here as they are built.
 */
val repositoryModule = module {
    // Example (uncomment when implementations exist):
    // single<MissionRepository> { MissionRepositoryImpl(get(), get()) }
    // single<AgentRepository> { AgentRepositoryImpl(get(), get()) }
    // single<LogRepository> { LogRepositoryImpl(get()) }
    // single<MemoryRepository> { MemoryRepositoryImpl(get()) }
}