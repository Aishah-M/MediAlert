package com.example.medialert.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.medialert.R
import com.example.medialert.theme.MediAlertTheme

@Composable
fun HomeScreen(
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()) {
        IconButton(
            onClick = onCallClick,
            modifier = Modifier.align(Alignment.Center) // Adjust alignment as needed
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_call_24),
                contentDescription = "Contact Screen"
            )
        }
    }
}
@Preview
@Composable
fun HomeScreenPreview() {
    MediAlertTheme {
        HomeScreen(
            onCallClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
