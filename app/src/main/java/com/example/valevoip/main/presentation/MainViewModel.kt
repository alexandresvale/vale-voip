package com.example.valevoip.main.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAccountUseCase: GetAccountUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData(MainUiState())
    val uiState: LiveData<MainUiState> = _uiState

    fun checkUseConfig() {
        viewModelScope.launch {
            val configModel = getAccountUseCase()
            setState {
                if (configModel != null) {
                    Log.d("####", "ConfigModel não é nulo = $configModel")
                    it.copy(hasUserConfig = true)
                } else {
                    Log.d("####", "ConfigModel é nulo = $configModel")
                    it.copy(hasUserConfig = false)
                }
            }
        }
    }

    private fun setState(update: (MainUiState) -> MainUiState) {
        val currentState = _uiState.value ?: MainUiState()
        _uiState.value = update(currentState)
    }
}