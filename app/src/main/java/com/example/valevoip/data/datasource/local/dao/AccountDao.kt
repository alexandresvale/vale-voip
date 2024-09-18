package com.example.valevoip.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.valevoip.data.datasource.local.entity.AccountEntity

@Dao
interface AccountDao {
    @Query("SELECT * FROM config WHERE id = 1")
    suspend fun getAccount(): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(accountEntity: AccountEntity)
}