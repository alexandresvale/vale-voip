package com.example.valevoip.presentation.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.valevoip.R
import com.example.valevoip.presentation.ui.theme.onPrimaryLight
import com.example.valevoip.presentation.ui.theme.primaryLight

@Composable
fun SplashLayout() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = primaryLight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_vale_voip_letras_brancas),
            contentDescription = "Logo Vale VoIP",
            modifier = Modifier
                .size(260.dp)
                .padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = onPrimaryLight
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun SplashLayoutPreview() {
    SplashLayout()
}