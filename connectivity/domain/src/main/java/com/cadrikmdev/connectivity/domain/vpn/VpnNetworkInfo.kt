package com.cadrikmdev.connectivity.domain.vpn

import com.cadrikmdev.connectivity.domain.NetworkInfo
import com.cadrikmdev.connectivity.domain.TransportType
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