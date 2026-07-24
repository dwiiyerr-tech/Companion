package com.hermes.companion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hermes.companion.core.domain.Agent
import com.hermes.companion.core.domain.Log
import com.hermes.companion.core.domain.Memory
import com.hermes.companion.core.domain.Mission

@Database(
    entities = [
        Mission::class,
        Agent::class,
        Log::class,
        Memory::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun missionDao(): MissionDao
    abstract fun agentDao(): AgentDao
    abstract fun logDao(): LogDao
    abstract fun memoryDao(): MemoryDao
}