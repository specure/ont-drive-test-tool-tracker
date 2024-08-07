package com.cadrikmdev.intercom.domain.client

data class DeviceNode(
    val address: String,
    val displayName: String,
    val isPaired: Boolean,
    val type: Int = -1,
)
