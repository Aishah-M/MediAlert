package com.example.medialert.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.medialert.R
import com.example.medialert.theme.MediAlertTheme

@Composable
fun ProfileEditScreen(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.mica2),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize()
    )
}
@Preview
@Composable
fun ProfileEditScreenPreview() {
    MediAlertTheme {
        ProfileEditScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}