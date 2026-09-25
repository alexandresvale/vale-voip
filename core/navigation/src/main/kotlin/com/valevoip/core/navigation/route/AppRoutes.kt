package com.valevoip.core.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

@Serializable
data object LoginRoute

@Serializable
data object HomeGraphRoute

@Serializable
data object HomeStartRoute


@Serializable
data class CallRoute(val number: String = "")

@Serializable
data object DialerRoute

@Serializable
data object HistoryRoute

@Serializable
data object SettingsRoute
