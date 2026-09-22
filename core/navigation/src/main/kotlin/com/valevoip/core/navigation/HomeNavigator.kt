package com.valevoip.core.navigation

/**
 * Contrato de navegação para a tela Home.
 * A implementação concreta é fornecida por feature:home via Hilt.
 */
interface HomeNavigator {
    fun navigateToHome(popUpFromRoute: String)
}