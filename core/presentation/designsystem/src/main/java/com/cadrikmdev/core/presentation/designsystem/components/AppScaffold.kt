package com.cadrikmdev.core.presentation.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cadrikmdev.core.presentation.designsystem.SignalTrackerTheme


@Composable
fun SignalTrackerScaffold(
    withGradient: Boolean = true,
    modifier: Modifier = Modifier,
    topAppBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topAppBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = FabPosition.Center,
        modifier = modifier,
    ) { paddingValues ->
        if (withGradient) {
            GradientBackground {
                content(paddingValues)
            }
        } else {
            content(paddingValues)
        }
    }
}


@Preview
@Composable
fun SignalTrackerScaffoldPreview() {
    SignalTrackerTheme {
        SignalTrackerScaffold(
            content = {

            }
        )
    }
}