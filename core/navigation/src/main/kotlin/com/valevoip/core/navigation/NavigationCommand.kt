package com.valevoip.core.navigation

/**
 * Representa todos os comandos de navegação disparáveis
 * de fora do Compose (Services, ViewModels, etc.).
 *
 * Adicione uma nova subclasse aqui sempre que um novo destino
 * precisar ser acionado por um componente externo ao NavHost.
 */
sealed class NavigationCommand {
    /** Abre a tela de chamada ativa com o número informado. */
    data class ToCall(val number: String) : NavigationCommand()
    /** Navega para a Home limpando o back stack de autenticação. */
    data object ToHome : NavigationCommand()
    /** Volta para a tela anterior. */
    data object Back : NavigationCommand()
}