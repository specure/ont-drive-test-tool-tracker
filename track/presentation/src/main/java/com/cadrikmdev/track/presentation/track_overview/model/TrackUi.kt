package com.cadrikmdev.track.presentation.track_overview.model

data class TrackUi(
    val id: String, // null if new run
    val duration: String,
    val dateTime: String,
    val distance: String,
    val avgSpeed: String,
    val maxSpeed: String,
    val pace: String,
    val totalElevation: String,
)
