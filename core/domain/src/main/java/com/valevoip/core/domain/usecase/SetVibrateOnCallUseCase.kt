package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.repository.SettingsRepository
import javax.inject.Inject

class SetVibrateOnCallUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.setVibrate(enabled)
    }
}
