package com.valevoip.core.analytics

interface AnalyticsTracker {
    fun logEvent(analyticsEvent: AnalyticsEvent)
    fun setUserProperty(propertyName: String, value: String)
}
