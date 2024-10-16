package com.specure.track.domain

import com.specure.core.domain.location.LocationWithDetails
import kotlinx.coroutines.flow.Flow

interface LocationObserver {
    fun observeLocation(interval: Long): Flow<LocationWithDetails>
}