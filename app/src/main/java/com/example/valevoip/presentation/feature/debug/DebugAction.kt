package com.example.valevoip.presentation.feature.debug

sealed class DebugAction {
    data object Register : DebugAction()
    data object Unregister : DebugAction()
    data object Retry : DebugAction()
    data object Call: DebugAction()
    data object IncomingCall: DebugAction()
}