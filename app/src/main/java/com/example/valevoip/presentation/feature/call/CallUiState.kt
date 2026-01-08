package com.example.valevoip.presentation.feature.call

import com.example.domain.model.CallStatus

data class CallUiState(
    val contactName: String = "Desconhecido",
    val contactNumber: String = "",
    val callStatus: CallStatus = CallStatus.IDLE,
    val duration: Long = 0L,
    val formattedDuration: String = "00:00",
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val contactPhotoUrl: String? = null
)