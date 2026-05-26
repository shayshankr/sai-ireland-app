package org.sathyasaieire.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.sathyasaieire.app.data.local.SaiDatabase
import org.sathyasaieire.app.data.local.dao.EventDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SaiDatabase =
        Room.databaseBuilder(context, SaiDatabase::class.java, "sai_ireland.db")
            .fallbackToDestructiveMigration() // safe during dev; add proper migrations before v1 release
            .build()

    @Provides
    fun provideEventDao(db: SaiDatabase): EventDao = db.eventDao()
}
