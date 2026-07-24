package com.hermes.companion.core.di

import androidx.room.Room
import com.hermes.companion.data.local.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            "hermes_companion.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().missionDao() }
    single { get<AppDatabase>().agentDao() }
    single { get<AppDatabase>().logDao() }
    single { get<AppDatabase>().memoryDao() }
}