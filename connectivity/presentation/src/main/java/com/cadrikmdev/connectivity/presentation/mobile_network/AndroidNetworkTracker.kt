package com.cadrikmdev.connectivity.presentation.mobile_network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.cadrikmdev.connectivity.domain.NetworkInfo
import com.cadrikmdev.connectivity.domain.NetworkTracker
import com.cadrikmdev.connectivity.domain.TransportType
import com.cadrikmdev.connectivity.domain.bluetooth.BluetoothNetworkInfo
import com.cadrikmdev.connectivity.domain.ethernet.EthernetNetworkInfo
import com.cadrikmdev.connectivity.domain.mobile.MobileNetworkInfo
import com.cadrikmdev.connectivity.domain.mobile.PrimaryDataSubscription
import com.cadrikmdev.connectivity.domain.vpn.VpnNetworkInfo
import com.cadrikmdev.connectivity.domain.wifi.WifiBand
import com.cadrikmdev.connectivity.domain.wifi.WifiNetworkInfo
import com.cadrikmdev.connectivity.presentation.mobile_network.util.filterOnlyPrimaryActiveDataCell
import com.cadrikmdev.connectivity.presentation.mobile_network.util.getCorrectDataTelephonyManagerOrNull
import com.cadrikmdev.connectivity.presentation.mobile_network.util.mapToMobileNetworkType
import com.cadrikmdev.connectivity.presentation.mobile_network.util.mccCompat
import com.cadrikmdev.connectivity.presentation.mobile_network.util.mncCompat
import com.cadrikmdev.connectivity.presentation.mobile_network.util.toSignalStrengthInfo
import cz.mroczis.netmonster.core.INetMonster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.UnknownHostException
import java.text.DecimalFormat

class AndroidNetworkTracker(
    private val context: Context,
    private val netmonster: INetMonster,
    private val telephonyManager: TelephonyManager,
    private val subscriptionManager: SubscriptionManager,
    private val connectivityManager: ConnectivityManager,
    private val wifiManager: WifiManager,
) : NetworkTracker {

    override fun observeNetwork(): Flow<List<NetworkInfo?>> {
        val dataEmitter = flow<List<NetworkInfo?>> {

            while (currentCoroutineContext().isActive) {
                if ((ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_PHONE_STATE
                    ) != PackageManager.PERMISSION_GRANTED) || (
                            (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED)
                            )
                ) {
                    emit(emptyList())
                } else {
                    val network = connectivityManager.activeNetwork
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

                    val networkType = when {
                        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> TransportType.WIFI
                        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> TransportType.CELLULAR
                        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) == true -> TransportType.BLUETOOTH
                        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> TransportType.ETHERNET
                        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> TransportType.VPN
                        else -> TransportType.UNKNOWN
                    }
                    val networkList: List<NetworkInfo> = when (networkType) {
                        TransportType.CELLULAR -> detectMobileNetwork(networkCapabilities.toString())
                        TransportType.WIFI -> detectWifiNetwork(networkCapabilities.toString())
                        TransportType.BLUETOOTH -> listOf(BluetoothNetworkInfo(networkCapabilities.toString()))
                        TransportType.ETHERNET -> listOf(EthernetNetworkInfo(networkCapabilities.toString()))
                        TransportType.VPN -> listOf(VpnNetworkInfo(networkCapabilities.toString()))
                        else -> {
                            emptyList<NetworkInfo>()
                        }
                    }

                    emit(networkList)
                }
                delay(700)
            }
        }

        return dataEmitter
    }

    private suspend fun detectWifiNetwork(
        networkCapabilitiesRaw: String?
    ): List<NetworkInfo> {
        return withContext(Dispatchers.IO) {
            val dummyWifiNetwork = listOf(WifiNetworkInfo(capabilitiesRaw = networkCapabilitiesRaw))

            if ((ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_WIFI_STATE
                ) != PackageManager.PERMISSION_GRANTED) || (
                        (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED)
                        )
            ) {
                return@withContext dummyWifiNetwork
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                //TODO: get wifi info for new android versions
                val activeNetwork = connectivityManager.activeNetwork
                connectivityManager.getNetworkCapabilities(activeNetwork)

                return@withContext dummyWifiNetwork
            } else {
                val info = wifiManager.connectionInfo ?: return@withContext dummyWifiNetwork

                val address = try {
                    val ipAddress = info.ipAddress.toBigInteger().toByteArray()
                    InetAddress.getByAddress(ipAddress).hostAddress
                } catch (ex: UnknownHostException) {
                    null
                }

                if (info.supplicantState == SupplicantState.DISCONNECTED || info.frequency == -1) {
                    return@withContext dummyWifiNetwork
                }

                val ssid = if (info.ssid == UNKNOWN_SSID || info.hiddenSSID) {
                    null
                } else {
                    info.ssid.removeQuotation() ?: ""
                }
                return@withContext listOf<NetworkInfo>(
                    WifiNetworkInfo(
                        bssid = if (info.bssid == DUMMY_MAC_ADDRESS || info.networkId == -1) null else info.bssid,
                        band = WifiBand.fromFrequency(info.frequency),
                        isSSIDHidden = info.hiddenSSID,
                        ipAddress = address,
                        linkSpeed = info.linkSpeed,
                        rxlinkSpeed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            info.rxLinkSpeedMbps
                        } else UNKNOWN,
                        txlinkSpeed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            info.txLinkSpeedMbps
                        } else UNKNOWN,
                        networkId = if (info.bssid == Companion.DUMMY_MAC_ADDRESS || info.networkId == -1) null else info.networkId,
                        rssi = info.rssi,
                        signalLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            wifiManager.calculateSignalLevel(info.rssi)
                        } else {
                            WifiManager.calculateSignalLevel(info.rssi, 5)
                        },
                        ssid = ssid,
                        supplicantState = (info.supplicantState
                            ?: SupplicantState.UNINITIALIZED).name,
                        supplicantDetailedState = (WifiInfo.getDetailedStateOf(info.supplicantState)
                            ?: android.net.NetworkInfo.DetailedState.IDLE).name,
                        signal = null,
                        capabilitiesRaw = networkCapabilitiesRaw
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun detectMobileNetwork(networkCapabilitiesRaw: String?): List<MobileNetworkInfo> {
        return withContext(Dispatchers.IO) {
            val defaultDataSubscriptionId =
                SubscriptionManager.getDefaultDataSubscriptionId()
            val isDefaultDataSubscriptionIdDetected =
                defaultDataSubscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID
            val simCount = subscriptionManager.activeSubscriptionInfoCount

            val mobileNetworkList =
                subscriptionManager.activeSubscriptionInfoList.map { subscriptionInfo ->
                    val netmonsterNetworkType =
                        netmonster.getNetworkType(subscriptionInfo.subscriptionId)
                    val mobileNetworkType = netmonsterNetworkType.mapToMobileNetworkType()

                    val validSubscriptionId =
                        subscriptionInfo.subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID

                    val isDefaultDataSubscription = when {
                        defaultDataSubscriptionId == subscriptionInfo.subscriptionId && validSubscriptionId && isDefaultDataSubscriptionIdDetected -> PrimaryDataSubscription.TRUE
                        defaultDataSubscriptionId != subscriptionInfo.subscriptionId && validSubscriptionId && isDefaultDataSubscriptionIdDetected -> PrimaryDataSubscription.FALSE
                        else -> PrimaryDataSubscription.UNKNOWN
                    }


                    val operatorName = subscriptionInfo.carrierName.toString()
                    val simOperatorMccMnc = when {
                        subscriptionInfo.mccCompat() == null -> null
                        subscriptionInfo.mncCompat() == null -> null
                        else -> "${subscriptionInfo.mccCompat()}-${
                            DecimalFormat("00").format(
                                subscriptionInfo.mncCompat()
                            )
                        }"
                    }
                    val simCountryIso = subscriptionInfo.countryIso
                    val simDisplayName = subscriptionInfo.displayName?.toString()

                    val primaryCellSignalDbm = netmonster.getCells()
                        .filterOnlyPrimaryActiveDataCell(dataSubscriptionId = subscriptionInfo.subscriptionId)
                        .firstOrNull()?.signal?.toSignalStrengthInfo(System.currentTimeMillis())?.value


                    MobileNetworkInfo(
                        name = operatorName,
                        simOperatorName = telephonyManager.getCorrectDataTelephonyManagerOrNull()?.simOperatorName.fixOperatorName(),
                        simDisplayName = simDisplayName,
                        simOperatorMccMnc = simOperatorMccMnc,
                        simCountryIso = simCountryIso,
                        networkType = mobileNetworkType,
                        operatorName = operatorName,
                        mcc = subscriptionInfo.mccCompat(),
                        mnc = subscriptionInfo.mncCompat(),
                        isRoaming = telephonyManager.getCorrectDataTelephonyManagerOrNull()?.isNetworkRoaming,
                        isPrimaryDataSubscription = isDefaultDataSubscription,
                        simCount = simCount,
                        primarySignalDbm = primaryCellSignalDbm,
                        capabilitiesRaw = networkCapabilitiesRaw,
                    )
                }
            mobileNetworkList
        }
    }

    private fun String?.fixOperatorName(): String? {
        return if (this == null) {
            null
        } else if (length >= 5 && !contains("-")) {
            "${substring(0, 3)}-${substring(3)}"
        } else {
            this
        }
    }

    private fun String?.removeQuotation(): String? {
        if (this != null && startsWith("\"") && endsWith("\""))
            return substring(1, length - 1)
        return this
    }

    companion object {
        private const val UNKNOWN_SSID = "<unknown ssid>"
        private const val DUMMY_MAC_ADDRESS = "02:00:00:00:00:00"
        private const val UNKNOWN = Int.MIN_VALUE
    }


}