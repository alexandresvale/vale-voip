package com.example.valevoip.data.datasource.local.mapper

import com.example.valevoip.data.datasource.local.entity.AccountEntity
import com.example.valevoip.domain.model.AccountModel

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