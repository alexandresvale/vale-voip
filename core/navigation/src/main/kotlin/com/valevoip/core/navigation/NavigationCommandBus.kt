package com.valevoip.core.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Canal central de comandos de navegação do app.
 *
 * Qualquer módulo (Service, ViewModel) pode emitir um [NavigationCommand]
 * sem conhecer o NavController ou qualquer detalhe do Compose.
 *
 * O [AppNavHost] observa esse canal via LaunchedEffect e executa
 * a navegação real através do NavController.
 *
 * extraBufferCapacity = 1: garante que um comando emitido antes do
 * NavHost estar pronto não seja silenciosamente descartado.
 */
@Singleton
class NavigationCommandBus @Inject constructor() {
    private val _commands = MutableSharedFlow<NavigationCommand>(extraBufferCapacity = 1)
    val commands: SharedFlow<NavigationCommand> = _commands.asSharedFlow()

    fun navigate(command: NavigationCommand) {
        _commands.tryEmit(command)
    }
}
