package com.hermes.companion.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hermes.companion.core.domain.Agent
import com.hermes.companion.core.domain.AgentStatus
import com.hermes.companion.core.domain.AgentType
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(agent: Agent)

    @Update
    suspend fun update(agent: Agent)

    @Query("DELETE FROM agents WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM agents WHERE id = :id")
    fun getById(id: String): Flow<Agent?>

    @Query("SELECT * FROM agents ORDER BY name")
    fun getAll(): Flow<List<Agent>>

    @Query("SELECT * FROM agents WHERE status = :status")
    fun getByStatus(status: AgentStatus): Flow<List<Agent>>

    @Query("SELECT * FROM agents WHERE type = :type")
    fun getByType(type: AgentType): Flow<List<Agent>>
}
