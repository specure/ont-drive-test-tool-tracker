package com.cadrikmdev.core.domain.location.service.model

data class LocationServiceStatus(
    val isGpsEnabled: Boolean,
    val isNetworkEnabled: Boolean,
    val isFusedEnabled: Boolean,
)
