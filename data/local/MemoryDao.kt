package com.hermes.companion.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hermes.companion.core.domain.Memory
import com.hermes.companion.core.domain.MemoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: Memory)

    @Query("SELECT * FROM memory WHERE id = :id")
    fun getById(id: String): Flow<Memory?>

    @Query("SELECT * FROM memory WHERE type = :type ORDER BY created_at DESC")
    fun getByType(type: MemoryType): Flow<List<Memory>>

    @Query("SELECT * FROM memory ORDER BY created_at DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<Memory>>

    @Query("SELECT * FROM memory WHERE type = :type ORDER BY created_at DESC LIMIT :limit")
    fun getRecentByType(type: MemoryType, limit: Int): Flow<List<Memory>>

    @Query("DELETE FROM memory WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM memory WHERE created_at < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long): Int
}
