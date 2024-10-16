package com.specure.connectivity.domain.ethernet

import com.specure.connectivity.domain.NetworkInfo
import com.specure.connectivity.domain.TransportType
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