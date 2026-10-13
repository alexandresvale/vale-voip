package com.valevoip.core.analytics

data class AnalyticsEvent(
    val eventName: String,
    val parameters: Map<String, Any>
)
