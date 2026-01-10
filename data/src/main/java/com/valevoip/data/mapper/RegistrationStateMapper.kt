package com.valevoip.data.mapper

import com.valevoip.domain.model.RegistrationStatus
import org.linphone.core.RegistrationState

fun RegistrationState.toDomain(): RegistrationStatus {
    return when (this) {
        RegistrationState.None -> RegistrationStatus.None
        RegistrationState.Progress -> RegistrationStatus.Progress
        RegistrationState.Ok -> RegistrationStatus.Ok
        RegistrationState.Cleared -> RegistrationStatus.Cleared
        RegistrationState.Failed -> RegistrationStatus.Failed
        RegistrationState.Refreshing -> RegistrationStatus.Refreshing
    }
}