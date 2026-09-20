package com.valevoip.data.local.mapper

import com.valevoip.data.local.entity.AccountEntity
import com.valevoip.core.domain.model.SipAccount

fun AccountEntity.toDomain() = SipAccount(
    username = username,
    password = password,
    domain = domain
)

fun SipAccount.toData() = AccountEntity(
    username = username,
    password = password,
    domain = domain
)