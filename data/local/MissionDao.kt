package com.hermes.companion.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hermes.companion.core.domain.Mission
import com.hermes.companion.core.domain.MissionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mission: Mission)

    @Update
    suspend fun update(mission: Mission)

    @Query("DELETE FROM missions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM missions WHERE id = :id")
    fun getById(id: String): Flow<Mission?>

    @Query("SELECT * FROM missions ORDER BY created_at DESC")
    fun getAll(): Flow<List<Mission>>

    @Query("SELECT * FROM missions WHERE status IN (:statuses)")
    fun getByStatus(statuses: List<MissionStatus>): Flow<List<Mission>>

    @Query("SELECT * FROM missions WHERE status = :status")
    fun getByStatus(status: MissionStatus): Flow<List<Mission>>

    @Query("DELETE FROM missions WHERE created_at < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long): Int
}
