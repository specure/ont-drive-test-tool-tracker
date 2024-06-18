package com.cadrikmdev.track.network

import kotlinx.serialization.Serializable

@Serializable
data class CreateTrackRequest(
    val durationMillis: Long,
    val epochMillis: Long,
    val lat: Double,
    val long: Double,
    val id: String
)
