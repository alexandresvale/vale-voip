package com.example.valevoip.feature.onboarding

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.valevoip.R

@Composable
fun OnboardingLayout() {
    Scaffold(
        content = {
            Column(
                modifier = Modifier
                    .padding(top = it.calculateTopPadding(), bottom = it.calculateBottomPadding())
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(color = Color.Red),
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_vale_voip),
                    contentDescription = stringResource(R.string.app_name),
                    Modifier.width(150.dp).height(150.dp),
                    alignment = Alignment.TopCenter
                )
                Text(text = stringResource(R.string.app_name))
            }
        }
    )
}

@Preview(showSystemUi = true)
@Composable
fun OnboardingLayoutPreview() {
    OnboardingLayout()
}