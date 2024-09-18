package com.example.valevoip.presentation.feature.onboarding.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.example.valevoip.presentation.ui.componet.TopBarUiVale


@Composable
fun OnboardingScreen(
    navHostController: NavHostController? = null
) {
    OnboardingContent()
}

@Composable
fun OnboardingContent() {
    Scaffold(
        topBar = {
            TopBarUiVale(
                title = "Onboarding",
                navigationBack = { }
            )
        },
        content = {
            Column(
                modifier = Modifier.padding(top = it.calculateTopPadding())
            ) {
                Text(text = "Alexandre")
            }
        },
    )
}

@Preview(showSystemUi = true)
@Composable
fun OnboardingContentPreview() {
    OnboardingContent()
}