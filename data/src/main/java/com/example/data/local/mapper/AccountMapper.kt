package com.example.data.local.mapper

import com.example.data.local.entity.AccountEntity
import com.example.domain.model.AccountModel

fun AccountEntity.toDomain() = AccountModel(
    username = username,
    password = password,
    serverDomain = serverDomain
)

fun AccountModel.toData() = AccountEntity(
    username = username,
    password = password,
    serverDomain = serverDomain
)