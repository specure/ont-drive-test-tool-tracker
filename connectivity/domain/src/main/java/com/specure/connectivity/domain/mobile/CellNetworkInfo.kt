/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.specure.connectivity.domain.mobile

import com.specure.connectivity.domain.NetworkInfo
import com.specure.connectivity.domain.SignalStrengthInfo
import com.specure.connectivity.domain.TransportType
import com.specure.connectivity.domain.mobile.band.CellBand


/**
 * Cellular Network information
 */
class CellNetworkInfo(

    /**
     * Provider or sim operator name
     */
    val providerName: String,

    /**
     * Cell band information of current network
     */
    val band: CellBand?,

    /**
     * Detailed Cellular Network type - it can be aggregated type for more cells like 5G NSA (which contains 5G and 4G cells)
     */
    val networkType: MobileNetworkType,

    /**
     * Detailed Cell type for the current network to know particular technology of the cell, if network uses more than one subtechnologies, as for 5G NSA it could be 5G and 4G cells together
     */
    val cellType: CellTechnology,

    val mnc: Int?,

    val mcc: Int?,

    val locationId: Long?,

    val areaCode: Int?,

    val scramblingCode: Int?,

    val isRegistered: Boolean,

    val isActive: Boolean,

    val isRoaming: Boolean,

    val apn: String?,

    val signalStrength: SignalStrengthInfo?,

    val dualSimDetectionMethod: String?,

    /**
     * additional information about network status because we can have NR cell but it is for NR NSA mode (where is more often LTE cell available)
     */
    val nrConnectionState: NrConnectionState,

    /**
     * Random generated cell UUID
     */
    cellUUID: String,

    val isPrimaryDataSubscription: PrimaryDataSubscription?,

    val cellState: String?,
) :
    NetworkInfo(
        TransportType.CELLULAR,
        timestampMillis = System.currentTimeMillis(),
        cellUUID,
        capabilitiesRaw = null
    ) {
    constructor(
        cellUUID: String,
    ) : this(
        mcc = null,
        mnc = null,
        providerName = "",
        band = null,
        networkType = MobileNetworkType.UNKNOWN,
        cellType = CellTechnology.CONNECTION_UNKNOWN,
        isRegistered = false,
        isActive = false,
        isRoaming = false,
        nrConnectionState = NrConnectionState.NOT_AVAILABLE,
        scramblingCode = null,
        apn = null,
        signalStrength = null,
        dualSimDetectionMethod = null,
        cellUUID = cellUUID,
        locationId = null,
        areaCode = null,
        isPrimaryDataSubscription = PrimaryDataSubscription.UNKNOWN,
        cellState = null
    )

    override val name: String?
        get() = providerName
}


fun Int?.fixValue(): Int? {
    return if (this == null || this == Int.MIN_VALUE || this == Int.MAX_VALUE || this < 0) {
        null
    } else {
        this
    }
}