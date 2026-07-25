package com.hermes.companion.core.di

import com.hermes.companion.data.repository.AgentRepository
import com.hermes.companion.data.repository.HermesRepository
import com.hermes.companion.data.repository.LogRepository
import com.hermes.companion.data.repository.MemoryRepository
import com.hermes.companion.data.repository.MissionRepository
import org.koin.dsl.module

/**
 * Repository DI module.
 * All repositories are singletons managed by Koin.
 */
val repositoryModule = module {
    // HermesRepository (needs Context -> provided by androidContext())
    single { HermesRepository(get(), null) }

    // MissionRepository depends on HermesRepository
    single { MissionRepository(get()) }

    // AgentRepository depends on HermesRepository
    single { AgentRepository(get()) }

    // LogRepository depends on HermesRepository
    single { LogRepository(get()) }

    // MemoryRepository depends on HermesRepository
    single { MemoryRepository(get()) }
}
