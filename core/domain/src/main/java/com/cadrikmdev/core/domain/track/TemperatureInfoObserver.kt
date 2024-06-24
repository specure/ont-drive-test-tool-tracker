package com.cadrikmdev.core.domain.track

import com.cadrikmdev.core.domain.Temperature
import kotlinx.coroutines.flow.Flow

interface TemperatureInfoObserver {
    fun observeTemperature(): Flow<Temperature?>

    fun register()

    fun unregister()
}