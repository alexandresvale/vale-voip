package com.valevoip.data.di

import android.content.Context
import androidx.room.Room
import com.valevoip.data.local.AppDatabase
import com.valevoip.data.local.dao.AccountDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "vale_voip_db")
            .fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideConfigDao(database: AppDatabase): AccountDao = database.accountDao()
}