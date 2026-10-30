package com.valevoip.feature.config

sealed interface ConfigUiEffect {
    data class ShareText(val report: String) : ConfigUiEffect
    data class ShowToast(val message: String) : ConfigUiEffect
}
