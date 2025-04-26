package com.example.valevoip.feature.debug

sealed class DebugAction {
    data object RegisterUser : DebugAction()
    data object FetchUser : DebugAction()
    data object Unregister : DebugAction()
    data object Retry : DebugAction()
    data object Call: DebugAction()
    data object IncomingCall: DebugAction()
}