package com.valevoip.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.valevoip.data.local.dao.AccountDao
import com.valevoip.data.local.entity.AccountEntity

@Database(
    version = 1,
    exportSchema = false,
    entities = [AccountEntity::class]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}