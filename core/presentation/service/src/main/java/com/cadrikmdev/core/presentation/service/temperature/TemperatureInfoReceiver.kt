package com.cadrikmdev.core.presentation.service.temperature

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.cadrikmdev.core.domain.Temperature
import com.cadrikmdev.core.domain.track.TemperatureInfoObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class TemperatureInfoReceiver(
    private val context: Context,
) : BroadcastReceiver(), TemperatureInfoObserver {
    // temperature in Celsius units in XXY format as XX.Y
    private var temp: Temperature? = null

    private val _temperatureFlow = MutableStateFlow<Temperature?>(null)
    val temperatureFlow: Flow<Temperature?> = _temperatureFlow.asStateFlow()

    override fun observeTemperature(): Flow<Temperature?> {
        return temperatureFlow
    }

    /**
     * temperature in Celzius or null if not acquired yet
     */
    fun getTemp(): Temperature? {
        return temp
    }

    // On samsung A526B (Android 14 - UpsideDownCake) it updates it values at static 30s intervals
    override fun onReceive(arg0: Context?, intent: Intent) {
        temp = Temperature(
            temperatureCelsius = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                .toDouble() / 10f,
            timestampMillis = System.currentTimeMillis()
        )
        val temperatureInCelsius = temp
        _temperatureFlow.tryEmit(temperatureInCelsius)
    }

    override fun register() {
        Timber.d("REGISTERING TEMPERATURE")
        context.registerReceiver(
            this,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    override fun unregister() {
        try {
            Timber.d("UNREGISTERING TEMPERATURE")
            context.unregisterReceiver(
                this
            )
        } catch (e: Exception) {
            Timber.e("Error during unregistering battery info receiver: ${e.localizedMessage}")
        }
    }

}