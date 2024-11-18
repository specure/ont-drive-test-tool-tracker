package com.specure.connectivity.presentation.mobile_network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.specure.connectivity.domain.NetworkInfo
import com.specure.connectivity.domain.NetworkTracker
import com.specure.connectivity.domain.TransportType
import com.specure.connectivity.domain.bluetooth.BluetoothNetworkInfo
import com.specure.connectivity.domain.ethernet.EthernetNetworkInfo
import com.specure.connectivity.domain.mobile.MobileNetworkInfo
import com.specure.connectivity.domain.mobile.PrimaryDataSubscription
import com.specure.connectivity.domain.vpn.VpnNetworkInfo
import com.specure.connectivity.domain.wifi.WifiBand
import com.specure.connectivity.domain.wifi.WifiNetworkInfo
import com.specure.connectivity.presentation.mobile_network.util.filterOnlyPrimaryActiveDataCell
import com.specure.connectivity.presentation.mobile_network.util.getCorrectDataTelephonyManagerOrNull
import com.specure.connectivity.presentation.mobile_network.util.mapToMobileNetworkType
import com.specure.connectivity.presentation.mobile_network.util.mccCompat
import com.specure.connectivity.presentation.mobile_network.util.mncCompat
import com.specure.connectivity.presentation.mobile_network.util.mobileNetworkType
import com.specure.connectivity.presentation.mobile_network.util.toCellNetworkInfo
import com.specure.connectivity.presentation.mobile_network.util.toSignalStrengthInfo
import cz.mroczis.netmonster.core.INetMonster
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber
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

    private var wifiCallback: ConnectivityManager.NetworkCallback? = null
    private val wifiRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()



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
                    val networkType = getNetworkType(networkCapabilities)
//                    prepareNetworkCallbacks(networkType)
                    val networkList: List<NetworkInfo> =
                        getNetworkInfoDetails(networkType, networkCapabilities)
                    emit(networkList)
                }
                delay(700)
            }
        }

        return dataEmitter
    }

    private suspend fun AndroidNetworkTracker.getNetworkInfoDetails(
        networkType: TransportType,
        networkCapabilities: NetworkCapabilities?
    ): List<NetworkInfo> {
        val networkList: List<NetworkInfo> = when (networkType) {
            TransportType.CELLULAR -> detectMobileNetwork(networkCapabilities.toString())
            TransportType.WIFI -> detectWifiNetwork(networkCapabilities.toString()) // TODO: this is not valid point for android S and higher if fine location is permitted and location services are on - process it in wifiCallback and store info
            TransportType.BLUETOOTH -> listOf(BluetoothNetworkInfo(networkCapabilities.toString()))
            TransportType.ETHERNET -> listOf(EthernetNetworkInfo(networkCapabilities.toString()))
            TransportType.VPN -> listOf(VpnNetworkInfo(networkCapabilities.toString()))
            else -> {
                emptyList<NetworkInfo>()
            }
        }
        return networkList
    }

    private fun prepareNetworkCallbacks(networkType: TransportType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (networkType == TransportType.WIFI) {
                registerWifiInfoCallback()
            } else {
                unregisterWifiInfoCallback()
            }
        }
    }

    private fun getNetworkType(networkCapabilities: NetworkCapabilities?) = when {
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> TransportType.WIFI
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> TransportType.CELLULAR
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) == true -> TransportType.BLUETOOTH
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> TransportType.ETHERNET
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> TransportType.VPN
        else -> TransportType.UNKNOWN
    }

    private suspend fun detectWifiNetwork(
        networkCapabilitiesRaw: String?
    ): List<NetworkInfo> {
        return withContext(Dispatchers.IO) {
            val basicWifiNetworkInfo = createBasicWifiNetworkInfo(networkCapabilitiesRaw)

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
                return@withContext basicWifiNetworkInfo
            }

            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val activeNetwork = connectivityManager.activeNetwork

                val wifiCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                var wifiInfo: WifiInfo?
                wifiCapabilities?.transportInfo.let {
                    wifiInfo = it as WifiInfo
                }
                registerWifiInfoCallback()

                wifiInfo
            } else {
                wifiManager.connectionInfo
            }

            val wifiInfo = processWifiInfo(info, networkCapabilitiesRaw)
            return@withContext wifiInfo
        }
    }

    private fun createBasicWifiNetworkInfo(networkCapabilitiesRaw: String?) =
        listOf(WifiNetworkInfo(capabilitiesRaw = networkCapabilitiesRaw))

    private fun processWifiInfo(
        info: WifiInfo?,
        networkCapabilitiesRaw: String?
    ): List<NetworkInfo> {
        if (info == null) {
            return createBasicWifiNetworkInfo(networkCapabilitiesRaw)
        }

        val address = try {
            val ipAddress = info.ipAddress.toBigInteger().toByteArray()
            InetAddress.getByAddress(ipAddress).hostAddress
        } catch (ex: UnknownHostException) {
            null
        }

        if (info.supplicantState == SupplicantState.DISCONNECTED || info.frequency == -1) {
            return createBasicWifiNetworkInfo(networkCapabilitiesRaw)
        }

        val ssid = if (info.ssid == UNKNOWN_SSID || info.hiddenSSID) {
            null
        } else {
            info.ssid.removeQuotation() ?: ""
        }
        return listOf<NetworkInfo>(
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

    private fun registerWifiInfoCallback() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (wifiCallback == null) {
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
                    wifiCallback = object : ConnectivityManager.NetworkCallback() {
                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: NetworkCapabilities
                        ) {
                            // TODO: process it correctly
                            super.onCapabilitiesChanged(network, networkCapabilities)
                            val wifiInfo = networkCapabilities.transportInfo as WifiInfo
                            val ssid = wifiInfo.ssid
                            val wifiNetworkInfo = processWifiInfo(
                                info = wifiInfo,
                                networkCapabilitiesRaw = networkCapabilities.toString()
                            )

                            Timber.d("WIFI SSID no location = ${ssid}")
                        }
                    }
                } else {
                    wifiCallback = object : ConnectivityManager.NetworkCallback(
                        FLAG_INCLUDE_LOCATION_INFO
                    ) {
                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: NetworkCapabilities
                        ) {
                            // TODO: process it correctly
                            super.onCapabilitiesChanged(network, networkCapabilities)
                            val wifiInfo = networkCapabilities.transportInfo as WifiInfo
                            val ssid = wifiInfo.ssid
                            Timber.d("WIFI SSID = ${ssid}")
                        }
                    }
                }
                wifiCallback?.let { networkCallback ->
                    connectivityManager.registerNetworkCallback(wifiRequest, networkCallback)
                    Timber.d("Wifi info callback registered")
                }
            }
        }
    }

    private fun unregisterWifiInfoCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            wifiCallback?.let { networkCallback ->
                connectivityManager.unregisterNetworkCallback(networkCallback)
                Timber.d("Wifi info callback unregistered")
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
                subscriptionManager.activeSubscriptionInfoList?.map { subscriptionInfo ->
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

                    val primaryDataCell = netmonster.getCells()
                        .filterOnlyPrimaryActiveDataCell(dataSubscriptionId = subscriptionInfo.subscriptionId)
                        .firstOrNull()

                    val primaryCellInfo = primaryDataCell?.toCellNetworkInfo(
                        apn = connectivityManager.activeNetworkInfo?.extraInfo,
                        dataTelephonyManager = telephonyManager.getCorrectDataTelephonyManagerOrNull()
                            ?: telephonyManager,
                        telephonyManagerNetmonster = NetMonsterFactory.getTelephony(
                            context,
                            primaryDataCell.subscriptionId
                        ),
                        primaryDataCell.mobileNetworkType(netmonster),
                        subscriptionInfo.subscriptionId,
                    )
                    val primaryCellSignalDbm =
                        primaryDataCell?.signal?.toSignalStrengthInfo(System.currentTimeMillis())?.value


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
                        primaryCell = primaryCellInfo,
                    )
                }
            mobileNetworkList ?: emptyList()
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