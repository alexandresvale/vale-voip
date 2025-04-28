package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AccountEntity

@Dao
interface AccountDao {
    @Query("SELECT * FROM account WHERE id = 1")
    suspend fun getAccount(): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(accountEntity: AccountEntity)

    @Query("DELETE FROM account")
    suspend fun clearAccount()
}