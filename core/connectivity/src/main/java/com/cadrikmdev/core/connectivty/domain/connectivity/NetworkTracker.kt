package com.cadrikmdev.core.connectivty.domain.connectivity

import kotlinx.coroutines.flow.Flow

interface NetworkTracker {
    fun observeNetwork(): Flow<List<NetworkInfo?>>
}