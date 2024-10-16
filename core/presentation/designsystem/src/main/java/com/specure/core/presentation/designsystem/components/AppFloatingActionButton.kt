package com.specure.core.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.specure.core.presentation.designsystem.SignalTrackerTheme
import com.specure.core.presentation.designsystem.TrackIcon

@Composable
fun SignalTrackerFloatingActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    iconSize: Dp = 25.dp,
    enabled: Boolean = true,
) {
    Box(
        modifier = Modifier
            .size(75.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            .clickable(onClick = if (enabled) {
                onClick
            } else {
                {}
            }),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        ButtonDefaults.buttonColors().disabledContainerColor
                    }
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    ButtonDefaults.buttonColors().disabledContentColor
                },
                modifier = Modifier.size(iconSize)
            )
        }
    }
}


@Preview
@Composable
fun SignalTrackerFloatingActionButtonPreview(

) {
    SignalTrackerTheme {
        SignalTrackerFloatingActionButton(
            icon = TrackIcon,
            enabled = false,
            onClick = {}
        )
    }
}