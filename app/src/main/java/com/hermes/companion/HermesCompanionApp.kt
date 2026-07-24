package com.hermes.companion

import android.app.Application
import com.hermes.companion.core.di.appModule
import com.hermes.companion.core.di.databaseModule
import com.hermes.companion.core.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class HermesCompanionApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidLogger()
            androidContext(this@HermesCompanionApp)
            modules(appModule, databaseModule, repositoryModule)
        }
    }

    companion object {
        lateinit var instance: HermesCompanionApp
            private set
    }
}