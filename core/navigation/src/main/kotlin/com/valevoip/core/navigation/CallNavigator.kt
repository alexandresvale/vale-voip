package com.valevoip.core.navigation

/**
 * Contrato de navegação para a tela de chamada ativa.
 * A implementação concreta é fornecida por feature:call via Hilt.
 */
interface CallNavigator {
    fun navigateToCall(number: String)
}