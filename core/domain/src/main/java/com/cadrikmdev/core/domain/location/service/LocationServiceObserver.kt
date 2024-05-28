package com.cadrikmdev.core.domain.location.service

import com.cadrikmdev.core.domain.location.service.model.LocationServiceStatus
import kotlinx.coroutines.flow.Flow

interface LocationServiceObserver {
    fun observeLocationServiceStatus(): Flow<LocationServiceStatus>
}