package com.example.spotify_clone.presentation.landing.widgets

import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomOutLinedButton(
    modifier: Modifier = Modifier,
    index: Int,
    onClick: (Int) -> Unit,
    child: @Composable () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        onClick = { onClick(index) }
    ) {
        child.invoke()
    }
}