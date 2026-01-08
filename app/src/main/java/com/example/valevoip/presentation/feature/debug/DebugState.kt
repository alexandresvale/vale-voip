package com.example.valevoip.presentation.feature.debug

data class DebugState(
    val isLoading: Boolean = false,
    val registrationStatus: String? = null,
    val errorMessage: String? = null,
    val message: String? = null,
    val isDialogOpen: Boolean = false
) {

    fun setLoadingState(isLoading: Boolean) = this.copy(
        isLoading = isLoading
    )

    fun setSuccessState(message: String) = this.copy(
        isLoading = false,
        message = message
    )

    fun setErrorState(errorMessage: String) = this.copy(
        isLoading = false,
        errorMessage = errorMessage,
        isDialogOpen = true
    )
}
