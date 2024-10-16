package com.specure.connectivity.presentation.network

import android.net.ConnectivityManager
import android.net.Network
import com.specure.connectivity.domain.ConnectivityObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.InetAddress


class NetworkConnectivityObserver(
    private val connectivityManager: ConnectivityManager
) : ConnectivityObserver {

    /**
     * This checks if internet connection is available as not every network connection has internet connection
     */
    override fun observerInternetAvailability(): Flow<Boolean> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch {
                        val isOnline = isInternetAvailable()
                        Timber.d("Sending onAvailable internet connection $isOnline")
                        send(isOnline)
                    }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch {
                        val isOnline = isInternetAvailable()
                        Timber.d("Sending onLosing internet connection $isOnline")
                        send(isOnline)
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch {
                        val isOnline = isInternetAvailable()
                        Timber.d("Sending onLost internet connection $isOnline")
                        send(isOnline)
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch {
                        val isOnline = isInternetAvailable()
                        Timber.d("Sending onUnavailable internet connection $isOnline")
                        send(isOnline)
                    }
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }

    override fun observeBasicConnectivity(): Flow<Boolean> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch {
                        Timber.d("Sending onAvailable true")
                        send(true)
                    }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch {
                        Timber.d("Sending onLosing true")
                        send(true)
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch {
                        Timber.d("Sending onLost false")
                        send(false)
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch {
                        Timber.d("Sending onUnavailable false")
                        send(false)
                    }
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }

    override fun observeDetailedConnectivity(): Flow<ConnectivityObserver.Status> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch {
                        send(ConnectivityObserver.Status.AVAILABLE)
                    }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch {
                        send(ConnectivityObserver.Status.LOSING)
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch {
                        send(ConnectivityObserver.Status.LOST)
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch {
                        send(ConnectivityObserver.Status.UNAVAILABLE)
                    }
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }

    private fun isInternetAvailable(): Boolean {
        return try {
            val ipAddr = InetAddress.getByName("google.com")
            //You can replace it with your name
            !ipAddr.hostAddress.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }
}