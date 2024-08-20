package com.cadrikmdev.core.domain.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

fun <T> Flow<T>.throttleLatest(periodMillis: Long): Flow<T> = channelFlow {
    var lastValue: T? = null
    var isThrottling = false

    collect { value ->
        lastValue = value
        if (!isThrottling) {
            isThrottling = true
            launch {
                delay(periodMillis)
                lastValue?.let { send(it) }
                isThrottling = false
            }
        }
    }
}
