package com.valevoip.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey val id: Int = 1,
    val username: String,
    val password: String,
    val domain: String
)