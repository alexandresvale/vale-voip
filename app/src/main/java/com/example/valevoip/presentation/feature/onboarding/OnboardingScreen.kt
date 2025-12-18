package com.example.valevoip.presentation.feature.onboarding

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun OnboardingScreen(
    onNavigateToDialer: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        Log.d("####", "OnboardingScreen LaunchedEffect")
        viewModel.effect.collect { effect ->
            when (effect) {
                is OnboardingEffect.NavigateToDialer -> {
                    onNavigateToDialer()
                }

                is OnboardingEffect.ShowErrorSnackBar -> {
                    snackBarHostState.showSnackbar(message = effect.message)
                }
            }
        }
    }

    OnboardingLayout(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Preview(showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(
        onNavigateToDialer = {}
    )
}