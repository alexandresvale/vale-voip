package com.valevoip.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.valevoip.core.navigation.HomeNavigator
import com.valevoip.core.navigation.NavigationCommand
import com.valevoip.core.navigation.NavigationCommandBus
import com.valevoip.feature.home.screen.HomeScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

const val HOME_GRAPH_ROUTE = "home_graph"
private const val HOME_START_ROUTE = "home_start"
const val HOME_ROUTE_DEEPLINK = "valevoip://home"

/**
 * Sub-grafo da tela principal com as abas internas.
 * O AppNavHost declara este bloco e fornece os callbacks de saída.
 */
fun NavGraphBuilder.mainGraph(
    onNavigateToCall: (String) -> Unit,
    nestedGraph: NavGraphBuilder.() -> Unit
) {
    navigation(
        startDestination = HOME_START_ROUTE,
        route = HOME_GRAPH_ROUTE
    ) {
        composable(
            route = HOME_START_ROUTE,
            deepLinks = listOf(navDeepLink { uriPattern = HOME_ROUTE_DEEPLINK })
        ) {
            HomeScreen(
                onNavigateToCall = onNavigateToCall,
                nestedGraph = nestedGraph
            )
        }
    }
}

@Module
@InstallIn((SingletonComponent::class))
object HomeNavigationModule {
    @Provides
    @Singleton
    fun provideHomeNavigation(navigationCommandBus: NavigationCommandBus): HomeNavigator {
        return object : HomeNavigator {
            override fun navigateToHome(popUpFromRoute: String) {
                navigationCommandBus.navigate(NavigationCommand.ToHome)
            }
        }
    }
}
