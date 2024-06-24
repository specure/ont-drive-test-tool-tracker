package com.cadrikmdev.connectivity.domain

import kotlinx.coroutines.flow.Flow

interface NetworkTracker {
    fun observeNetwork(): Flow<List<NetworkInfo?>>
}