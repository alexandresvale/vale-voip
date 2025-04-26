package com.example.valevoip.ui.componet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun CircularProgress(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    if (isLoading) {
        Row(
            modifier = modifier.clickable {  }
                .clickable(enabled = false) {}
                .background(color = Color.Gray, shape = RectangleShape)
                .fillMaxSize()
                .wrapContentHeight()
                .wrapContentWidth()

        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(65.dp)
                    .height(65.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }

}

@Preview(showBackground = true, showSystemUi = true, device = Devices.NEXUS_5X)
@Composable
fun CircularProgressPreview() {
    CircularProgress(isLoading = true)
}