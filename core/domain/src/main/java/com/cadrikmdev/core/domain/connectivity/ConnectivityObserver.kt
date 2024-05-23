package com.cadrikmdev.core.domain.connectivity

import kotlinx.coroutines.flow.Flow


interface ConnectivityObserver {

    fun observerInternetAvailability(): Flow<Boolean>
    fun observeBasicConnectivity(): Flow<Boolean>

    fun observeDetailedConnectivity(): Flow<Status>

    enum class Status {
        AVAILABLE,
        UNAVAILABLE,
        LOSING,
        LOST
    }
}