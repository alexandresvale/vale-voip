package com.valevoip.feature.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.valevoip.core.designsystem.theme.ValeVoipTheme
import com.valevoip.core.domain.model.SipRegistrationState
import com.valevoip.feature.home.HomeUiState
import com.valevoip.feature.home.model.BottomBarScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeLayout(
    state: HomeUiState,
    currentDestination: NavDestination?,
    onBottomItemClick: (BottomBarScreen<*>) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val currentScreen = bottomNavItems.find { screen ->
        currentDestination?.hasRoute(screen.route::class) == true
    } ?: BottomBarScreen.Dialer

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
                currentDestination = currentDestination,
                onItemClick = onBottomItemClick
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
private fun ConnectionStatusIndicator(
    state: SipRegistrationState,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusText) = when (state) {
        is SipRegistrationState.Ok -> Color(0xFF4CAF50) to "Conectado"
        is SipRegistrationState.Progress,
        is SipRegistrationState.Refreshing -> Color(0xFFFF9800) to "Conectando..."

        is SipRegistrationState.Failed -> Color(0xFFF44336) to "Desconectado"
        is SipRegistrationState.Cleared,
        is SipRegistrationState.None -> Color(0xFF9E9E9E) to "Desconectado"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(statusColor.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = statusColor
        )
    }
}

@Composable
private fun BottomNavigationBar(
    currentDestination: NavDestination?,
    onItemClick: (BottomBarScreen<*>) -> Unit = {},
) {
    NavigationBar {
        bottomNavItems.forEach { screen ->
            val isSelected = currentDestination?.hasRoute(screen.route::class) == true
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
            currentDestination = null,
            onBottomItemClick = {},
            content = {}
        )
    }
}