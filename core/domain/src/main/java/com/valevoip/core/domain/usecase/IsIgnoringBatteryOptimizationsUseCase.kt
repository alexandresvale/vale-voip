package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.client.BatteryOptimizationProvider
import javax.inject.Inject

class IsIgnoringBatteryOptimizationsUseCase @Inject constructor(
    private val batteryOptimizationProvider: BatteryOptimizationProvider
) {
    operator fun invoke(): Boolean = batteryOptimizationProvider.isIgnoringBatteryOptimizations
}
