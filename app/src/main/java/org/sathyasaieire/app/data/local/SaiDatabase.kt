package org.sathyasaieire.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.sathyasaieire.app.data.local.dao.EventDao
import org.sathyasaieire.app.data.local.entity.EventEntity

@Database(
    entities = [EventEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SaiDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}
