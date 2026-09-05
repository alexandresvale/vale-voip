package com.valevoip.feature.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.feature.home.HomeUiState
import com.valevoip.feature.home.model.BottomBarScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeLayout(
    state: HomeUiState,
    currentRoute: String?,
    onBottomItemClick: (BottomBarScreen) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val currentScreen = bottomNavItems.find { it.route == currentRoute } ?: BottomBarScreen.Dialer

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentScreen.title,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    ConnectionStatusIndicator(
                        state = state.connectionState,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
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
private fun ConnectionStatusIndicator(state: SipRegistrationState, modifier: Modifier = Modifier) {
    val color = when (state) {
        is SipRegistrationState.Ok -> Color(0xFF4CAF50)
        is SipRegistrationState.Progress,
        is SipRegistrationState.Refreshing -> Color(0xFFFFC107)

        is SipRegistrationState.Failed -> Color(0xFFF44336)
        else -> Color.LightGray
    }
    Box(
        modifier = modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun BottomNavigationBar(
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
internal fun HomeLayoutPreview() {
    ValeVoipTheme() {
        HomeLayout(
            state = HomeUiState(),
            currentRoute = null,
            onBottomItemClick = {},
            content = {}
        )
    }
}