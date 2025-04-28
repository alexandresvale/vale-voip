package com.example.valevoip.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AccountModel
import com.example.domain.usecase.RegisterUserUseCase
import com.example.valevoip.domain.usecase.InsertAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val insertAccountUseCase: InsertAccountUseCase,
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun saveConfig(configModel: AccountModel) {
        viewModelScope.launch {
            insertAccountUseCase(configModel)
        }
    }
}