package com.specure.core.presentation.service.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import com.specure.core.domain.location.service.LocationServiceObserver
import com.specure.core.domain.location.service.model.LocationServiceStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

class AndroidLocationServiceObserver(
    private val context: Context
) : LocationServiceObserver {

    override fun observeLocationServiceStatus(): Flow<LocationServiceStatus> {
        return callbackFlow {
            val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

            val receiver = object : BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                        val isFusedEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)
                        } else {
                            false
                        }
                        Timber.d("Location service status: $isGpsEnabled")
                        trySend(
                            LocationServiceStatus(
                                isGpsEnabled,
                                isNetworkEnabled,
                                isFusedEnabled
                            )
                        )
                    }
                }
            }

            val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
            context.registerReceiver(receiver, filter)

            awaitClose { context.unregisterReceiver(receiver) }
        }
    }
}