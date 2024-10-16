package com.specure.connectivity.domain.vpn

import com.specure.connectivity.domain.NetworkInfo
import com.specure.connectivity.domain.TransportType
import java.util.UUID

class VpnNetworkInfo(

    override val capabilitiesRaw: String?

) : NetworkInfo(
    TransportType.VPN,
    System.currentTimeMillis(),
    UUID.nameUUIDFromBytes(("").toByteArray()).toString(),
    capabilitiesRaw
) {

    override val name: String?
        get() = null
}