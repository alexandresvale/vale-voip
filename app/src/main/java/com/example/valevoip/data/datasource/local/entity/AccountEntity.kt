package com.example.valevoip.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "config")
data class AccountEntity(
    @PrimaryKey val id: Int = 1,
    val username: String,
    val password: String,
    val serverDomain: String
)