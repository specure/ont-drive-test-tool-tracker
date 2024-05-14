package com.cadrikmdev.run.presentation.run_overview

data class RunUi(
    val id: String, // null if new run
    val duration: String,
    val dateTime: String,
    val distance: String,
    val avgSpeed: String,
    val maxSpeed: String,
    val pace: String,
    val totalElevation: String,
    val mapPictureUrl: String?
)
