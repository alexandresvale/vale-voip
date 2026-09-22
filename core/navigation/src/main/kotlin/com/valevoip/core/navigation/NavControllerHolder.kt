package com.valevoip.core.navigation

import androidx.navigation.NavController
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda uma referência ao NavController principal do app.
 * Deve ser inicializado na MainActivity e limpo no onDestroy.
 *
 * Retorna null quando o app está morto — nesse caso, o Deep Link
 * da notificação faz o papel de abrir o app.
 */
@Singleton
class NavControllerHolder @Inject constructor() {
    private var navController: NavController? = null

    fun set(navController: NavController) {
        this.navController = navController
    }

    fun get(): NavController? = navController

    fun clear() {
        navController = null
    }
}
