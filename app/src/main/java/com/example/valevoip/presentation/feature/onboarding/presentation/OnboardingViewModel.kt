package com.example.valevoip.presentation.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.valevoip.domain.model.AccountModel
import com.example.valevoip.domain.usecase.InsertAccountUseCase
import com.example.valevoip.domain.usecase.RegisterAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val insertAccountUseCase: InsertAccountUseCase,
    private val registerAccountUseCase: RegisterAccountUseCase
) : ViewModel() {

    fun saveConfig(configModel: AccountModel) {
        viewModelScope.launch {
            insertAccountUseCase(configModel)
        }
    }
}