package com.valevoip.app.presentation.feature.main.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.valevoip.app.presentation.feature.main.MainNavGraph
import com.valevoip.app.presentation.feature.main.MainUiState
import com.valevoip.app.presentation.feature.main.MainViewModel
import com.valevoip.app.presentation.feature.main.model.BottomBarScreen
import com.valevoip.app.presentation.ui.theme.ValeVoipTheme

val bottomNavItems = listOf(
    BottomBarScreen.Dialer,
    BottomBarScreen.History,
    BottomBarScreen.Settings
)

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onNavigateToCall: (String) -> Unit
) {
    val state by mainViewModel.uiState.collectAsStateWithLifecycle()
    val bottomNavController = rememberNavController()

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    MainLayout(
        state = state,
        currentRoute = currentRoute,
        onBottomItemClick = { screen ->
            bottomNavController.navigate(screen.route) {
                popUpTo(bottomNavController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                MainNavGraph(
                    navController = bottomNavController,
                    onNavigateToCall = onNavigateToCall
                )
            }
        }
    )
}

@Composable
fun MainLayout(
    state: MainUiState,
    currentRoute: String?,
    onBottomItemClick: (BottomBarScreen) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onItemClick = onBottomItemClick
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onItemClick: (BottomBarScreen) -> Unit = {},
) {

    NavigationBar {
        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                label = { Text(text = screen.title) },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onItemClick(screen)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.iconSelected else screen.icon,
                        contentDescription = screen.title
                    )
                }
            )
        }
    }
}

@Preview
@Composable
fun MainContentPreview() {
    ValeVoipTheme() {
        MainLayout(
            state = MainUiState(),
            currentRoute = null,
            onBottomItemClick = {},
            content = {}
        )
    }
}