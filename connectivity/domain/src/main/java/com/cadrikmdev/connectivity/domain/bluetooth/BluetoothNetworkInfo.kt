package com.cadrikmdev.connectivity.domain.bluetooth

import com.cadrikmdev.connectivity.domain.NetworkInfo
import com.cadrikmdev.connectivity.domain.TransportType
import java.util.UUID

class BluetoothNetworkInfo(

    override val capabilitiesRaw: String?

) : NetworkInfo(
    TransportType.BLUETOOTH,
    System.currentTimeMillis(),
    UUID.nameUUIDFromBytes(("").toByteArray()).toString(),
    capabilitiesRaw
) {

    override val name: String?
        get() = null
}