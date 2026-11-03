package com.valevoip.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

/**
 * CompositionLocal que permite que telas filhas (como o Histórico) injetem
 * ações dinâmicas (como OverflowMenu) diretamente na TopAppBar do Scaffold principal,
 * mantendo as features 100% desacopladas da Home (Clean Architecture & DIP).
 */
val LocalTopBarActions = compositionLocalOf<MutableState<(@Composable () -> Unit)?>> {
    mutableStateOf(null)
}
