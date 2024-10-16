package com.specure.connectivity.domain

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