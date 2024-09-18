package com.example.valevoip.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.valevoip.data.datasource.local.dao.AccountDao
import com.example.valevoip.data.datasource.local.entity.AccountEntity

@Database(
    version = 1,
    exportSchema = false,
    entities = [AccountEntity::class]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}