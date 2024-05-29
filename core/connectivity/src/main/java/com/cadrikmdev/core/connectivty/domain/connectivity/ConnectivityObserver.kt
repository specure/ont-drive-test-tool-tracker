package com.cadrikmdev.core.connectivty.domain.connectivity

import kotlinx.coroutines.flow.Flow


interface ConnectivityObserver {

    fun observerInternetAvailability(): Flow<Boolean>
    fun observeBasicConnectivity(): Flow<Boolean>

    fun observeDetailedConnectivity(): Flow<com.cadrikmdev.core.connectivty.domain.connectivity.ConnectivityObserver.Status>

    enum class Status {
        AVAILABLE,
        UNAVAILABLE,
        LOSING,
        LOST
    }
}