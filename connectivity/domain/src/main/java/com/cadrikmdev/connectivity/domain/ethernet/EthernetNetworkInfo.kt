package com.cadrikmdev.connectivity.domain.ethernet

import com.cadrikmdev.connectivity.domain.NetworkInfo
import com.cadrikmdev.connectivity.domain.TransportType
import java.util.UUID

class EthernetNetworkInfo(

    override val capabilitiesRaw: String?

) : NetworkInfo(
    TransportType.ETHERNET,
    System.currentTimeMillis(),
    UUID.nameUUIDFromBytes(("").toByteArray()).toString(),
    capabilitiesRaw
) {

    override val name: String?
        get() = null
}