package com.cadrikmdev.run.domain

import com.cadrikmdev.core.domain.locaiton.LocationWithAltitude
import kotlinx.coroutines.flow.Flow

interface LocationObserver {
    fun observeLocation(interval: Long): Flow<LocationWithAltitude>
}