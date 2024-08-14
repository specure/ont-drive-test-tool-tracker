package com.cadrikmdev.intercom.domain.client

data class TrackingDevice(
    val name: String,
    val address: String,
    val status: String,
    val connected: Boolean,
    val updateTimestamp: Long,
)
