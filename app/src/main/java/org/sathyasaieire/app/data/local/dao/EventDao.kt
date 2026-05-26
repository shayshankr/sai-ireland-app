package org.sathyasaieire.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.sathyasaieire.app.data.local.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE dateTimeMs >= :now ORDER BY dateTimeMs ASC")
    fun getUpcoming(now: Long = System.currentTimeMillis()): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE dateTimeMs < :now ORDER BY dateTimeMs DESC")
    fun getPast(now: Long = System.currentTimeMillis()): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY dateTimeMs ASC")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: String): EventEntity?

    @Upsert
    suspend fun upsertAll(events: List<EventEntity>)

    @Upsert
    suspend fun upsert(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM events")
    suspend fun deleteAll()
}
