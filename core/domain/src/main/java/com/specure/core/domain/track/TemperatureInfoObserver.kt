package com.specure.core.domain.track

import com.specure.core.domain.Temperature
import kotlinx.coroutines.flow.Flow

interface TemperatureInfoObserver {
    fun observeTemperature(): Flow<Temperature?>

    fun register()

    fun unregister()
}