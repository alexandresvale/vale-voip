package com.example.valevoip.navigation

sealed class Destinations(val route: String) {
    data object Onboarding : Destinations("onboarding")
    data object Main : Destinations("main")
    data object Dialer : Destinations("dialer")
    data object History : Destinations("history")
    data object Debug : Destinations("debug")
}