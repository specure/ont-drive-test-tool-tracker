package com.cadrikmdev.track.domain

import com.cadrikmdev.core.domain.location.LocationWithDetails
import kotlinx.coroutines.flow.Flow

interface LocationObserver {
    fun observeLocation(interval: Long): Flow<LocationWithDetails>
}