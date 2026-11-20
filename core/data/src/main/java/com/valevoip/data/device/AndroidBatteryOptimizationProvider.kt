package com.valevoip.data.device

import android.content.Context
import android.os.PowerManager
import com.valevoip.core.domain.client.BatteryOptimizationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidBatteryOptimizationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : BatteryOptimizationProvider {

    private val powerManager: PowerManager? =
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    override val isIgnoringBatteryOptimizations: Boolean
        get() = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
}
