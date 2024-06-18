package com.cadrikmdev.core.connectivty.presentation.mobile_network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.cadrikmdev.core.connectivty.domain.connectivity.NetworkInfo
import com.cadrikmdev.core.connectivty.domain.connectivity.NetworkTracker
import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.MobileNetworkInfo
import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.PrimaryDataSubscription
import com.cadrikmdev.core.connectivty.presentation.mobile_network.util.filterOnlyPrimaryActiveDataCell
import com.cadrikmdev.core.connectivty.presentation.mobile_network.util.getCorrectDataTelephonyManagerOrNull
import com.cadrikmdev.core.connectivty.presentation.mobile_network.util.mapToMobileNetworkType
import com.cadrikmdev.core.connectivty.presentation.mobile_network.util.mccCompat
import com.cadrikmdev.core.connectivty.presentation.mobile_network.util.mncCompat
import com.cadrikmdev.core.connectivty.presentation.mobile_network.util.toSignalStrengthInfo
import cz.mroczis.netmonster.core.INetMonster
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.text.DecimalFormat

class NetmonsterNetworkTracker(
    private val context: Context,
    private val netmonster: INetMonster,
    private val telephonyManager: TelephonyManager,
    private val subscriptionManager: SubscriptionManager,
    private val connectivityManager: ConnectivityManager,
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

                            val primaryCellSignalDbm = netmonster.getCells().filterOnlyPrimaryActiveDataCell(dataSubscriptionId = subscriptionInfo.subscriptionId).firstOrNull()?.signal?.toSignalStrengthInfo(System.currentTimeMillis())?.value

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
                                obtainedTimestampMillis = System.currentTimeMillis(),
                                primarySignalDbm = primaryCellSignalDbm
                            )
                        }
                    emit(mobileNetworkList)
                }
                delay(700)
            }
        }

        return dataEmitter
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


}