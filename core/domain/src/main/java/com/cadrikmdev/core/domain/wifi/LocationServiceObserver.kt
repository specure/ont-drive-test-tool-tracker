package com.cadrikmdev.core.domain.wifi

import kotlinx.coroutines.flow.Flow

interface WifiServiceObserver {
    fun observeWifiServiceEnabledStatus(): Flow<Boolean>
}