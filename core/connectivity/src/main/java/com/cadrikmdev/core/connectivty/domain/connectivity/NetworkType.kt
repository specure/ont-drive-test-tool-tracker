package com.cadrikmdev.core.connectivty.domain.connectivity

import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.CellTechnology
import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.MobileNetworkType


enum class NetworkType(val stringValue: String, val minSignalValue: Int, val maxSignalValue: Int) {

    TYPE_2G("2G",
        SignalStrengthInfo.CELLULAR_SIGNAL_MIN,
        SignalStrengthInfo.CELLULAR_SIGNAL_MAX
    ),
    TYPE_3G("3G",
        SignalStrengthInfo.WCDMA_RSRP_SIGNAL_MIN,
        SignalStrengthInfo.WCDMA_RSRP_SIGNAL_MAX
    ),
    TYPE_4G("4G",
        SignalStrengthInfo.LTE_RSRP_SIGNAL_MIN,
        SignalStrengthInfo.LTE_RSRP_SIGNAL_MAX
    ),
    TYPE_5G("5G",
        SignalStrengthInfo.NR_RSRP_SIGNAL_MIN,
        SignalStrengthInfo.NR_RSRP_SIGNAL_MAX
    ),
    TYPE_5G_NSA("5G",
        SignalStrengthInfo.NR_RSRP_SIGNAL_MIN,
        SignalStrengthInfo.NR_RSRP_SIGNAL_MAX
    ),
    TYPE_5G_AVAILABLE("4G (+5G)",
        SignalStrengthInfo.LTE_RSRP_SIGNAL_MIN,
        SignalStrengthInfo.LTE_RSRP_SIGNAL_MAX
    ),
    TYPE_WLAN("WLAN",
        SignalStrengthInfo.WIFI_MIN_SIGNAL_VALUE,
        SignalStrengthInfo.WIFI_MAX_SIGNAL_VALUE
    ),
    TYPE_LAN("ETHERNET", Int.MIN_VALUE, Int.MIN_VALUE),
    TYPE_BROWSER("BROWSER", Int.MIN_VALUE, Int.MIN_VALUE),
    TYPE_UNKNOWN("UNKNOWN", Int.MIN_VALUE, Int.MIN_VALUE),
    TYPE_BLUETOOTH("BLUETOOTH", Int.MIN_VALUE, Int.MIN_VALUE),
    TYPE_VPN("VPN", Int.MIN_VALUE, Int.MIN_VALUE);

    companion object {

        fun fromResultIntType(value: Int): NetworkType {

            if (value == Int.MAX_VALUE) {
                NetworkType.TYPE_2G
            }

            var cellTechnology: CellTechnology? = null
            val mobileNetworkType = MobileNetworkType.fromValue(value)
            val transportType = if (mobileNetworkType == MobileNetworkType.UNKNOWN) {
                when (value) {
                    TYPE_ETHERNET_VALUE -> TransportType.ETHERNET
                    TYPE_WIFI_VALUE -> TransportType.WIFI
                    TYPE_BROWSER_VALUE -> TransportType.BROWSER
                    TYPE_BLUETOOTH_VALUE -> TransportType.BLUETOOTH
                    TYPE_VPN_VALUE -> TransportType.VPN
                    else -> {
                        null
                    }
                }
            } else {
                cellTechnology = CellTechnology.fromMobileNetworkType(mobileNetworkType)
                TransportType.CELLULAR
            }

            return Companion.fromType(
                transportType,
                cellTechnology
            )
        }

        fun fromString(value: String): NetworkType {
            values().forEach {
                if (it.stringValue.equals(value, ignoreCase = true)) return it
            }

            com.cadrikmdev.core.connectivty.domain.connectivity.ServerNetworkType.values().forEach {
                if (it.stringValue.equals(value, ignoreCase = true) && it.compatType != null) return it.compatType
            }
            return NetworkType.TYPE_UNKNOWN
//            throw IllegalArgumentException("Failed to find NetworkTypeCompat for value $value")
        }

        fun fromType(transportType: TransportType?, cellTechnology: CellTechnology? = null): NetworkType {
            return if (transportType == null) {
                NetworkType.TYPE_UNKNOWN
            } else when (transportType) {
                TransportType.BROWSER -> NetworkType.TYPE_BROWSER
                TransportType.ETHERNET -> NetworkType.TYPE_LAN
                TransportType.WIFI -> NetworkType.TYPE_WLAN
                TransportType.BLUETOOTH -> NetworkType.TYPE_BLUETOOTH
                TransportType.VPN -> NetworkType.TYPE_VPN
                TransportType.CELLULAR -> {
                    when (cellTechnology) {
                        CellTechnology.CONNECTION_2G -> NetworkType.TYPE_2G
                        CellTechnology.CONNECTION_3G -> NetworkType.TYPE_3G
                        CellTechnology.CONNECTION_4G -> NetworkType.TYPE_4G
                        CellTechnology.CONNECTION_4G_5G -> NetworkType.TYPE_5G_AVAILABLE
                        CellTechnology.CONNECTION_5G -> NetworkType.TYPE_5G
                        else -> {
                            NetworkType.TYPE_UNKNOWN
                        }
                    }
                }

                else -> {
                    NetworkType.TYPE_UNKNOWN
                }
            }
        }

        const val TYPE_VPN_VALUE = 108
        const val TYPE_BLUETOOTH_VALUE = 107
        const val TYPE_ETHERNET_VALUE = 106
        const val TYPE_WIFI_VALUE = 99
        const val TYPE_BROWSER_VALUE = 98
    }
}

enum class ServerNetworkType(
    val intValue: Int,
    val stringValue: String,
    val compatType: NetworkType?,
    val transportType: TransportType?,
    val mobileNetworkType: MobileNetworkType?
) {
    TYPE_2G_GSM(1, "2G (GSM)",
        NetworkType.TYPE_2G,
        TransportType.CELLULAR, MobileNetworkType.GSM
    ),
    TYPE_2G_EDGE(2, "2G (EDGE)",
        NetworkType.TYPE_2G,
        TransportType.CELLULAR, MobileNetworkType.EDGE
    ),
    TYPE_3G_UMTS(3, "3G (UMTS)",
        NetworkType.TYPE_3G,
        TransportType.CELLULAR, MobileNetworkType.UMTS
    ),
    TYPE_2G_CDMA(4, "2G (CDMA)",
        NetworkType.TYPE_2G,
        TransportType.CELLULAR, MobileNetworkType.CDMA
    ),
    TYPE_2G_EVDO_0(5, "2G (EVDO_0)",
        NetworkType.TYPE_2G,
        TransportType.CELLULAR, MobileNetworkType.EVDO_0
    ),
    TYPE_2G_EVDO_A(6, "2G (EVDO_A)",
        NetworkType.TYPE_2G,
        TransportType.CELLULAR, MobileNetworkType.EVDO_A
    ),
    TYPE_2G_1xRTT(7, "2G (1xRTT)",
        NetworkType.TYPE_2G,
        TransportType.CELLULAR, MobileNetworkType._1xRTT
    ),
    TYPE_3G_HSDPA(8, "3G (HSDPA)",
        NetworkType.TYPE_3G,
        TransportType.CELLULAR, MobileNetworkType.HSDPA
    ),
    TYPE_3G_HSUPA(9, "3G (HSUPA)",
        NetworkType.TYPE_3G,
        TransportType.CELLULAR, MobileNetworkType.HSUPA
    ),
    TYPE_3G_HSPA(10, "3G (HSPA)",
        NetworkType.TYPE_3G,
        TransportType.CELLULAR, MobileNetworkType.HSPA
    ),
    TYPE_2G_IDEN(11, "2G (IDEN)",
        NetworkType.TYPE_2G,
        TransportType.CELLULAR, MobileNetworkType.IDEN
    ),
    TYPE_2G_EVDO_B(12, "2G (EVDO_B)",
        NetworkType.TYPE_2G,
        TransportType.CELLULAR, MobileNetworkType.EVDO_B
    ),
    TYPE_4G_LTE(13, "4G (LTE)",
        NetworkType.TYPE_4G,
        TransportType.CELLULAR, MobileNetworkType.LTE
    ),
    TYPE_2G_EHRPD(14, "2G (EHRPD)",
        NetworkType.TYPE_2G,
        TransportType.CELLULAR, MobileNetworkType.EHRPD
    ),
    TYPE_3G_HSPA_P(15, "3G (HSPA+)",
        NetworkType.TYPE_3G,
        TransportType.CELLULAR, MobileNetworkType.HSPAP
    ),
    TYPE_4G_LTE_CA(19, "4G (LTE CA)",
        NetworkType.TYPE_4G,
        TransportType.CELLULAR, MobileNetworkType.LTE_CA
    ),
    TYPE_5G_NR(20, "5G (NR)",
        NetworkType.TYPE_5G,
        TransportType.CELLULAR, MobileNetworkType.NR_SA
    ),
    TYPE_5G_NR_NSA(41, "5G (NSA)",
        NetworkType.TYPE_5G_NSA,
        TransportType.CELLULAR, MobileNetworkType.NR_NSA
    ),
    TYPE_5G_NR_AVAILABLE(
        40,
        "4G+(5G)",
        NetworkType.TYPE_5G_NSA,
        TransportType.CELLULAR,
        MobileNetworkType.NR_AVAILABLE
    ),
    TYPE_CLI(97, "CLI", null, null, null),
    TYPE_BROWSER(98, "BROWSER", null, null, null),
    TYPE_WLAN(99, "WLAN",
        NetworkType.TYPE_WLAN,
        TransportType.WIFI, null
    ),
    TYPE_2G_3G(101, "2G/3G",
        NetworkType.TYPE_3G,
        TransportType.CELLULAR, MobileNetworkType.HSUPA
    ),
    TYPE_3G_4G(102, "3G/4G",
        NetworkType.TYPE_3G,
        TransportType.CELLULAR, MobileNetworkType.HSUPA
    ),
    TYPE_2G_4G(103, "2G/4G",
        NetworkType.TYPE_4G,
        TransportType.CELLULAR, MobileNetworkType.EVDO_A
    ),
    TYPE_2G_3G_4G(104, "2G/3G/4G",
        NetworkType.TYPE_4G,
        TransportType.CELLULAR, MobileNetworkType.LTE
    ),
    TYPE_MOBILE(105, "MOBILE",
        NetworkType.TYPE_3G,
        TransportType.CELLULAR, MobileNetworkType.HSUPA
    ),
    TYPE_ETHERNET(106, "Ethernet",
        NetworkType.TYPE_LAN,
        TransportType.ETHERNET, null
    ),
    TYPE_BLUETOOTH(107, "Bluetooth", null,
        TransportType.BLUETOOTH, null
    ),
    TYPE_VPN(108, "VPN", null, TransportType.VPN, null),
    TYPE_UNKNOWN(-1, "UNKNOWN", null, null, null),
    TYPE_UNKNOWN2(Int.MAX_VALUE, "UNKNOWN", null, null, null),
    TYPE_UNKNOWN_BACKEND(0, "MOBILE", null, null, null) // used for mobile connection but unknown type of it
}