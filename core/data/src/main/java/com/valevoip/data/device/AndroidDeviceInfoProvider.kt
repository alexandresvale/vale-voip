package com.valevoip.data.device

import android.content.Context
import android.os.Build
import com.valevoip.core.domain.client.DeviceInfoProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidDeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceInfoProvider {

    override val appVersion: String by lazy {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Desconhecida"
        } catch (e: Exception) {
            "Desconhecida"
        }
    }

    override val manufacturer: String = Build.MANUFACTURER
    override val model: String = Build.MODEL
    override val osVersion: String = Build.VERSION.RELEASE
    override val apiLevel: Int = Build.VERSION.SDK_INT
}
