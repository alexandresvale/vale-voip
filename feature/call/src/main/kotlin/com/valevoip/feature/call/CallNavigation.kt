package com.valevoip.feature.call

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.valevoip.core.navigation.CallNavigator
import com.valevoip.core.navigation.NavigationCommand
import com.valevoip.core.navigation.NavigationCommandBus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

const val CALL_ROUTE = "call_route/{number}"
const val CALL_ROUTE_DEEPLINK = "valevoip://call/{number}"

fun NavGraphBuilder.callScreen(onNavigateBack: () -> Unit) {
    composable(
        route = CALL_ROUTE,
        deepLinks = listOf(navDeepLink { uriPattern = CALL_ROUTE_DEEPLINK })
    ) { backStackEntry ->
        val number = backStackEntry.arguments?.getString("number") ?: ""
        CallScreen(onNavigateBack = onNavigateBack)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object CallNavigatorModule {
    @Provides
    @Singleton
    fun provideCallNavigator(navigationCommandBus: NavigationCommandBus): CallNavigator {
        return object : CallNavigator {
            override fun navigateToCall(number: String) {
                navigationCommandBus.navigate(NavigationCommand.ToCall(number))
            }
        }
    }
}
