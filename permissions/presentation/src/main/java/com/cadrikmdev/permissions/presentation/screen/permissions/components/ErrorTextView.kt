package com.cadrikmdev.permissions.presentation.screen.permissions.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes

@Composable
fun ErrorTextView(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Preview(showSystemUi = true)
@PreviewScreenSizes
@Composable
fun PreviewErrorTextView() {
    ErrorTextView(
        "Password is too short"
    )
}
