package com.cadrikmdev.core.connectivty.domain.connectivity

import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.CellTechnology
import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.MobileNetworkType


enum class NetworkType(val stringValue: String, val minSignalValue: Int, val maxSignalValue: Int) {

    TYPE_2G("2G",
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.CELLULAR_SIGNAL_MIN,
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.CELLULAR_SIGNAL_MAX
    ),
    TYPE_3G("3G",
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.WCDMA_RSRP_SIGNAL_MIN,
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.WCDMA_RSRP_SIGNAL_MAX
    ),
    TYPE_4G("4G",
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.LTE_RSRP_SIGNAL_MIN,
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.LTE_RSRP_SIGNAL_MAX
    ),
    TYPE_5G("5G",
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.NR_RSRP_SIGNAL_MIN,
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.NR_RSRP_SIGNAL_MAX
    ),
    TYPE_5G_NSA("5G",
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.NR_RSRP_SIGNAL_MIN,
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.NR_RSRP_SIGNAL_MAX
    ),
    TYPE_5G_AVAILABLE("4G (+5G)",
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.LTE_RSRP_SIGNAL_MIN,
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.LTE_RSRP_SIGNAL_MAX
    ),
    TYPE_WLAN("WLAN",
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.WIFI_MIN_SIGNAL_VALUE,
        com.cadrikmdev.core.connectivty.domain.connectivity.SignalStrengthInfo.Companion.WIFI_MAX_SIGNAL_VALUE
    ),
    TYPE_LAN("ETHERNET", Int.MIN_VALUE, Int.MIN_VALUE),
    TYPE_BROWSER("BROWSER", Int.MIN_VALUE, Int.MIN_VALUE),
    TYPE_UNKNOWN("UNKNOWN", Int.MIN_VALUE, Int.MIN_VALUE),
    TYPE_BLUETOOTH("BLUETOOTH", Int.MIN_VALUE, Int.MIN_VALUE),
    TYPE_VPN("VPN", Int.MIN_VALUE, Int.MIN_VALUE);

    companion object {

        fun fromResultIntType(value: Int): com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType {

            if (value == Int.MAX_VALUE) {
                com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_2G
            }

            var cellTechnology: CellTechnology? = null
            val mobileNetworkType = MobileNetworkType.fromValue(value)
            val transportType = if (mobileNetworkType == MobileNetworkType.UNKNOWN) {
                when (value) {
                    com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.Companion.TYPE_ETHERNET_VALUE -> com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.ETHERNET
                    com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.Companion.TYPE_WIFI_VALUE -> com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.WIFI
                    com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.Companion.TYPE_BROWSER_VALUE -> com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.BROWSER
                    com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.Companion.TYPE_BLUETOOTH_VALUE -> com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.BLUETOOTH
                    com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.Companion.TYPE_VPN_VALUE -> com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.VPN
                    else -> {
                        null
                    }
                }
            } else {
                cellTechnology = CellTechnology.fromMobileNetworkType(mobileNetworkType)
                com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR
            }

            return com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.Companion.fromType(
                transportType,
                cellTechnology
            )
        }

        fun fromString(value: String): com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType {
            values().forEach {
                if (it.stringValue.equals(value, ignoreCase = true)) return it
            }

            com.cadrikmdev.core.connectivty.domain.connectivity.ServerNetworkType.values().forEach {
                if (it.stringValue.equals(value, ignoreCase = true) && it.compatType != null) return it.compatType
            }
            return com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_UNKNOWN
//            throw IllegalArgumentException("Failed to find NetworkTypeCompat for value $value")
        }

        fun fromType(transportType: com.cadrikmdev.core.connectivty.domain.connectivity.TransportType?, cellTechnology: CellTechnology? = null): com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType {
            return if (transportType == null) {
                com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_UNKNOWN
            } else when (transportType) {
                com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.BROWSER -> com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_BROWSER
                com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.ETHERNET -> com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_LAN
                com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.WIFI -> com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_WLAN
                com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.BLUETOOTH -> com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_BLUETOOTH
                com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.VPN -> com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_VPN
                com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR -> {
                    when (cellTechnology) {
                        CellTechnology.CONNECTION_2G -> com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_2G
                        CellTechnology.CONNECTION_3G -> com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_3G
                        CellTechnology.CONNECTION_4G -> com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_4G
                        CellTechnology.CONNECTION_4G_5G -> com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_5G_AVAILABLE
                        CellTechnology.CONNECTION_5G -> com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_5G
                        else -> {
                            com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_UNKNOWN
                        }
                    }
                }

                else -> {
                    com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_UNKNOWN
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
    val compatType: com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType?,
    val transportType: com.cadrikmdev.core.connectivty.domain.connectivity.TransportType?,
    val mobileNetworkType: MobileNetworkType?
) {
    TYPE_2G_GSM(1, "2G (GSM)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_2G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.GSM),
    TYPE_2G_EDGE(2, "2G (EDGE)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_2G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.EDGE),
    TYPE_3G_UMTS(3, "3G (UMTS)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_3G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.UMTS),
    TYPE_2G_CDMA(4, "2G (CDMA)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_2G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.CDMA),
    TYPE_2G_EVDO_0(5, "2G (EVDO_0)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_2G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.EVDO_0),
    TYPE_2G_EVDO_A(6, "2G (EVDO_A)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_2G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.EVDO_A),
    TYPE_2G_1xRTT(7, "2G (1xRTT)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_2G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType._1xRTT),
    TYPE_3G_HSDPA(8, "3G (HSDPA)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_3G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.HSDPA),
    TYPE_3G_HSUPA(9, "3G (HSUPA)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_3G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.HSUPA),
    TYPE_3G_HSPA(10, "3G (HSPA)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_3G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.HSPA),
    TYPE_2G_IDEN(11, "2G (IDEN)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_2G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.IDEN),
    TYPE_2G_EVDO_B(12, "2G (EVDO_B)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_2G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.EVDO_B),
    TYPE_4G_LTE(13, "4G (LTE)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_4G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.LTE),
    TYPE_2G_EHRPD(14, "2G (EHRPD)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_2G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.EHRPD),
    TYPE_3G_HSPA_P(15, "3G (HSPA+)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_3G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.HSPAP),
    TYPE_4G_LTE_CA(19, "4G (LTE CA)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_4G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.LTE_CA),
    TYPE_5G_NR(20, "5G (NR)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_5G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.NR_SA),
    TYPE_5G_NR_NSA(41, "5G (NSA)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_5G_NSA,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.NR_NSA),
    TYPE_5G_NR_AVAILABLE(
        40,
        "4G+(5G)",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_5G_NSA,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR,
        MobileNetworkType.NR_AVAILABLE
    ),
    TYPE_CLI(97, "CLI", null, null, null),
    TYPE_BROWSER(98, "BROWSER", null, null, null),
    TYPE_WLAN(99, "WLAN",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_WLAN,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.WIFI, null),
    TYPE_2G_3G(101, "2G/3G",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_3G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.HSUPA),
    TYPE_3G_4G(102, "3G/4G",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_3G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.HSUPA),
    TYPE_2G_4G(103, "2G/4G",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_4G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.EVDO_A),
    TYPE_2G_3G_4G(104, "2G/3G/4G",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_4G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.LTE),
    TYPE_MOBILE(105, "MOBILE",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_3G,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.CELLULAR, MobileNetworkType.HSUPA),
    TYPE_ETHERNET(106, "Ethernet",
        com.cadrikmdev.core.connectivty.domain.connectivity.NetworkType.TYPE_LAN,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.ETHERNET, null),
    TYPE_BLUETOOTH(107, "Bluetooth", null,
        com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.BLUETOOTH, null),
    TYPE_VPN(108, "VPN", null, com.cadrikmdev.core.connectivty.domain.connectivity.TransportType.VPN, null),
    TYPE_UNKNOWN(-1, "UNKNOWN", null, null, null),
    TYPE_UNKNOWN2(Int.MAX_VALUE, "UNKNOWN", null, null, null),
    TYPE_UNKNOWN_BACKEND(0, "MOBILE", null, null, null) // used for mobile connection but unknown type of it
}