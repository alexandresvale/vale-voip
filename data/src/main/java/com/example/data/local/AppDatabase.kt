package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.local.entity.AccountEntity

@Database(
    version = 1,
    exportSchema = false,
    entities = [AccountEntity::class]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}