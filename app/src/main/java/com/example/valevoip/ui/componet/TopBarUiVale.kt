package com.example.valevoip.ui.componet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarUiVale(
    title: String,
    modifier: Modifier = Modifier,
    navigationBack: (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title
            )
        },
        navigationIcon = {
            navigationBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        },
        colors = TopAppBarColors(
            containerColor = Color.Blue,
            scrolledContainerColor = Color.Red,
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = modifier
    )

}

@Preview
@Composable
fun TopBarUiValePreview() {
    TopBarUiVale(
        title = "Onboarding",
        navigationBack = {}
    )
}