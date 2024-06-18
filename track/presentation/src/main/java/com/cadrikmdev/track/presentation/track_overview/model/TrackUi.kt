package com.cadrikmdev.track.presentation.track_overview.model

data class TrackUi(
    val id: String, // null if new run
    val duration: String,
    val dateTime: String,
)
