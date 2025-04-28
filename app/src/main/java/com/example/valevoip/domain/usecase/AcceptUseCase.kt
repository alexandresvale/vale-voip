package com.example.valevoip.domain.usecase

import com.example.valevoip.data.SipAccountRepository

class AcceptUseCase(
    private val repository: SipAccountRepository
){
    suspend operator fun invoke() = repository.accept()
}