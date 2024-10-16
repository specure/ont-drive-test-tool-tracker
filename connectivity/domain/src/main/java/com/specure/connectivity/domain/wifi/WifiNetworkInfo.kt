package com.specure.connectivity.domain.wifi

import com.specure.connectivity.domain.NetworkInfo
import com.specure.connectivity.domain.TransportType
import java.util.UUID

class WifiNetworkInfo(
    /**
     * Return the basic service set identifier (BSSID) of the current access point.
     * The BSSID in the form of a six-byte MAC address: {@code XX:XX:XX:XX:XX:XX},
     * may be {@code null} if there is no network currently connected.
     */
    val bssid: String? = null,

    /**
     * Wifi Band and frequency information
     */
    val band: WifiBand? = null,

    /**
     * {@code true} if this network does not broadcast its SSID, so an
     * SSID-specific probe request must be used for scans.
     */
    val isSSIDHidden: Boolean? = null,

    /**
     * Device IP address in WiFi network
     */
    val ipAddress: String? = null,

    /**
     * The current link speed in Mbps.
     */
    val linkSpeed: Int? = null,

    /**
     * The current RX link speed in Mbps.
     */
    val rxlinkSpeed: Int? = null,

    /**
     * The current TX link speed in Mbps.
     */
    val txlinkSpeed: Int? = null,

    /**
     * Each configured network has a unique small integer ID, used to identify
     * the network when performing operations on the supplicant. This method
     * returns the ID for the currently connected network.
     * Network ID, or null if there is no currently connected network or data is not available
     */
    val networkId: Int? = null,

    /**
     * Returns the received signal strength indicator of the current 802.11
     * network, in dBm.
     */
    val rssi: Int? = null,

    /**
     * Wifi signal level in range from 0 to 100
     */
    val signalLevel: Int? = null,

    /**
     * Returns the service set identifier (SSID) of the current 802.11 network.
     * If the SSID can be decoded as UTF-8, it will be returned surrounded by double
     * quotation marks. Otherwise, it is returned as a string of hex digits. The
     * SSID may be &lt;unknown ssid&gt; if there is no network currently connected,
     * or if the caller has insufficient permissions to access the SSID.
     */
    val ssid: String? = null,

    /**
     * Return the state of the supplicant's negotiation with an
     * access point, in the form of a [android.net.wifi.SupplicantState] object represented as a String.
     */
    val supplicantState: String? = null,

    /** Return the detailed state of the supplicant's negotiation with an
     * access point, in the form of a [android.net.NetworkInfo.DetailedState] object represented as a String.
     */
    val supplicantDetailedState: String? = null,

    /**
     * Raw signal assigned later when onSignalStrength change will trigger update
     */
    var signal: Int? = null,

    override val capabilitiesRaw: String?

) : NetworkInfo(
    TransportType.WIFI,
    System.currentTimeMillis(),
    UUID.nameUUIDFromBytes((ssid ?: "").toByteArray()).toString(),
    capabilitiesRaw
) {

    override val name: String?
        get() = ssid
}