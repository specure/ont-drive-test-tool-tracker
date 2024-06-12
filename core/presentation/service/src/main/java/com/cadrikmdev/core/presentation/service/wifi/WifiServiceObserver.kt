package com.cadrikmdev.core.presentation.service.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import com.cadrikmdev.core.domain.wifi.WifiServiceObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

class AndroidWifiServiceObserver(
    private val context: Context
) : WifiServiceObserver {

    override fun observeWifiServiceEnabledStatus(): Flow<Boolean> {
        return callbackFlow {
            val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager

            val receiver = object : BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                        val isWifiEnabled = wifiManager.isWifiEnabled
                        Timber.d("Location service status: $isWifiEnabled")
                        trySend(
                            isWifiEnabled
                        )
                    }
                }
            }

            val filter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
            context.registerReceiver(receiver, filter)

            awaitClose { context.unregisterReceiver(receiver) }
        }
    }
}