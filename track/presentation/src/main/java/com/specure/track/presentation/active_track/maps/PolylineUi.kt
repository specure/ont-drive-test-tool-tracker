package com.specure.track.presentation.active_track.maps

import androidx.compose.ui.graphics.Color
import com.specure.core.domain.location.Location

data class PolylineUi(
    val location1: Location,
    val location2: Location,
    val color: Color
)