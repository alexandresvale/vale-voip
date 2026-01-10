package com.valevoip.data.local.mapper

import com.valevoip.data.local.entity.AccountEntity
import com.valevoip.domain.model.AccountModel

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