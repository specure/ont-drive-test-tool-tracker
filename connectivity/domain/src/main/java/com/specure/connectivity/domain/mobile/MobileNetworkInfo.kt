package com.specure.connectivity.domain.mobile

import com.specure.connectivity.domain.NetworkInfo
import com.specure.connectivity.domain.TransportType
import java.util.UUID

data class MobileNetworkInfo(
    override val name: String?,
    /**
     * Detailed Cellular Network type - it can be aggregated type for more cells like 5G NSA (which contains 5G and 4G cells)
     */
    val networkType: MobileNetworkType,

    /**
     * Provider or sim operator name (this is user editable)
     */
    val simDisplayName: String?,
    /**
     * Provider or sim operator name (this is not user editable)
     */
    val simOperatorName: String?,
    val simOperatorMccMnc: String?,
    val simCountryIso: String?,
    /**
     * obtained from subscription - info from network
     */
    val operatorName: String?,
    val mnc: Int?,
    val mcc: Int?,
    val isRoaming: Boolean?,
    val isPrimaryDataSubscription: PrimaryDataSubscription?,
    val simCount: Int,

    val primarySignalDbm: Int?,
    val primaryCell: CellNetworkInfo?,
    override val capabilitiesRaw: String?

) : NetworkInfo(
    TransportType.CELLULAR,
    System.currentTimeMillis(),
    UUID.randomUUID().toString(),
    capabilitiesRaw
)

