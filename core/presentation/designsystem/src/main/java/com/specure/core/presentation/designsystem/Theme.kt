package com.specure.core.presentation.designsystem

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val DarkColorScheme = darkColorScheme(
    primary = SignalTrackerBlue,
    background = SignalTrackerBlack,
    surface = SignalTrackerDarkGray,
    secondary = SignalTrackerWhite,
    tertiary = SignalTrackerWhite,
    primaryContainer = SignalTrackerBlue30,
    onPrimary = SignalTrackerBlack,
    onBackground = SignalTrackerWhite,
    onSurface = SignalTrackerWhite,
    onSurfaceVariant = SignalTrackerGray,
    error = SignalTrackerDarkRed,
    errorContainer = SignalTrackerDarkRed5,
)

@Composable
fun SignalTrackerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}