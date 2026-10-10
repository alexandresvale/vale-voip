package com.valevoip.core.analytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject

class CrashlyticsTree @Inject constructor() : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) return

        val crashlytics = FirebaseCrashlytics.getInstance()
        val breadcrumb = if (tag != null) "[$tag] $message" else message

        crashlytics.log(breadcrumb)
        if (t != null) crashlytics.recordException(t)
    }
}
