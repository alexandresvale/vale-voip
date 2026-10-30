package com.valevoip.feature.config

sealed interface ConfigUiAction {
    data class SetEchoCancellation(val enabled: Boolean) : ConfigUiAction
    data class SetVibrate(val enabled: Boolean) : ConfigUiAction
    data class SetWifiOnly(val enabled: Boolean) : ConfigUiAction
    data object OpenAudioTest : ConfigUiAction
    data object CloseAudioTest : ConfigUiAction
    data object StartAudioTest : ConfigUiAction
    data object StopAudioTest : ConfigUiAction
    data object OpenClearHistoryDialog : ConfigUiAction
    data object DismissClearHistoryDialog : ConfigUiAction
    data object ConfirmClearHistory : ConfigUiAction
    data object ShareDiagnosticReport : ConfigUiAction
}
