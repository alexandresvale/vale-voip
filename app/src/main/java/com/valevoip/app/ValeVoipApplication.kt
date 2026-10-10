package com.valevoip.app

import android.app.Application
import android.content.pm.ApplicationInfo
import com.valevoip.core.analytics.CrashlyticsTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

const val VALEVOIP_TAG = "VALEVOIP"

@HiltAndroidApp
class ValeVoipApplication : Application() {

    @Inject
    lateinit var crashlyticsTree: CrashlyticsTree

    override fun onCreate() {
        super.onCreate()
        setupLogging()
    }

    private fun setupLogging() {
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    val newMessage = if (tag != null) "$tag - $message" else message
                    super.log(priority, VALEVOIP_TAG, newMessage, t)
                }
            })
            Timber.plant(crashlyticsTree)
        } else {
            Timber.plant(crashlyticsTree)
        }
    }
}
