package com.example.valevoip.di

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.service.LinphoneManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLinphoneManager(application: Application): LinphoneManager {
        return LinphoneManager(context = application)
    }

    @Provides
    @Singleton
    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, "vale_database")
            .fallbackToDestructiveMigration()
            .build()
    }
}