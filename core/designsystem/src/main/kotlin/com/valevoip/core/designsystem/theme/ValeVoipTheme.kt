package com.valevoip.core.designsystem.theme

import androidx.compose.runtime.Composable

object ValeVoipTheme {
    val extendedColors: ValeVoipExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}