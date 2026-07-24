package com.hermes.companion.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hermes.companion.core.domain.Log
import com.hermes.companion.core.domain.LogLevel
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: Log)

    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<Log>>

    @Query("SELECT * FROM logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getSince(since: Long): Flow<List<Log>>

    @Query("SELECT * FROM logs WHERE level = :level ORDER BY timestamp DESC LIMIT :limit")
    fun getByLevel(level: LogLevel, limit: Int): Flow<List<Log>>

    @Query("SELECT * FROM logs WHERE source = :source ORDER BY timestamp DESC LIMIT :limit")
    fun getBySource(source: String, limit: Int): Flow<List<Log>>

    @Query("DELETE FROM logs WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long): Int

    @Query("DELETE FROM logs WHERE id NOT IN (SELECT id FROM logs ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun prune(limit: Int): Int
}
