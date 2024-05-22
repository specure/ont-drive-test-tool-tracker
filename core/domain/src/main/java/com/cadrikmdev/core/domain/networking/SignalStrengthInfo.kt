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

package com.cadrikmdev.core.domain.networking

import kotlin.math.abs

/**
 * Class that contains data about signal strength
 */

abstract class SignalStrengthInfo {
    /**
     * Transport type of network
     */
    abstract val transport: TransportType

    /**
     * Signal strength in dBm
     */
    abstract val value: Int?

    /**
     * RSRQ in db
     */
    abstract val rsrq: Int?

    /**
     * Signal level in range 0..4
     */
    abstract val signalLevel: Int

    /**
     * Minimum signal value for current network type in dBm
     */
    abstract val min: Int

    /**
     * Maximum signal value for current network type in dBm
     */
    abstract val max: Int

    /**
     * Timestamp in nanoseconds when data was received
     */
    abstract val timestampNanos: Long


    companion object {

        const val SIGNAL_UNAVAILABLE = Int.MAX_VALUE

        const val WIFI_MIN_SIGNAL_VALUE = -100
        const val WIFI_MAX_SIGNAL_VALUE = -30

        const val CELLULAR_SIGNAL_MIN = -110
        const val CELLULAR_SIGNAL_MAX = -50

        const val LTE_RSRP_SIGNAL_MIN = -130
        const val LTE_RSRP_SIGNAL_MAX = -70

        const val CDMA_RSRP_SIGNAL_MIN = -120
        const val CDMA_RSRP_SIGNAL_MAX = -24

        const val WCDMA_RSRP_SIGNAL_MIN = -120
        const val WCDMA_RSRP_SIGNAL_MAX = -24

        const val TDSCDMA_RSRP_SIGNAL_MIN = -120
        const val TDSCDMA_RSRP_SIGNAL_MAX = -24

        const val NR_RSRP_SIGNAL_MIN = -130 // dbm
        const val NR_RSRP_SIGNAL_MAX = -70

        const val NR_RSRQ_SIGNAL_MIN = -20 // dbm
        const val NR_RSRQ_SIGNAL_MAX = -3 // values taken from CellSignalStrengthNr

        const val NR_SINR_SIGNAL_MIN = -23 // dbm
        const val NR_SINR_SIGNAL_MAX = 40 // values taken from CellSignalStrengthNr

        // Lifted from Default carrier configs and max range of SSRSRP from
        // mSsRsrpThresholds array
        // Boundaries: [-140 dB, -44 dB]
        const val SSRSRP_SIGNAL_STRENGTH_NONE = -130
        const val SSRSRP_SIGNAL_STRENGTH_POOR = -110
        const val SSRSRP_SIGNAL_STRENGTH_MODERATE = -90
        const val SSRSRP_SIGNAL_STRENGTH_GOOD = -70


        fun calculateCellSignalLevel(signal: Int?, min: Int, max: Int): Int {
            val relativeSignal: Double = ((signal ?: 0) - min.toDouble()) / (max - min)
            return when {
                relativeSignal <= 0.0 -> 0
                relativeSignal < 0.25 -> 1
                relativeSignal < 0.5 -> 2
                relativeSignal < 0.75 -> 3
                else -> 4
            }
        }

        fun calculateNRSignalLevel(signalValue: Int?): Int {
            return when {
                signalValue == null -> 0
                signalValue <= SSRSRP_SIGNAL_STRENGTH_NONE -> 0
                signalValue < SSRSRP_SIGNAL_STRENGTH_POOR -> 1
                signalValue < SSRSRP_SIGNAL_STRENGTH_MODERATE -> 2
                signalValue < SSRSRP_SIGNAL_STRENGTH_GOOD -> 3
                else -> 4
            }
        }

        private fun Int?.fixRssnr(): Int? {
            return if (this == null) {
                null
            } else {
                var value = -1 * abs(this)
                if (value < NR_RSRP_SIGNAL_MIN || value > NR_RSRP_SIGNAL_MAX || this == Int.MIN_VALUE) {
                    null
                } else {
                    this
                }
            }
        }

        private fun Int?.fixNrRsrp(): Int? {
            return if (this == null) {
                null
            } else {
                var value = -1 * abs(this)
                if (value < NR_RSRP_SIGNAL_MIN || value > NR_RSRP_SIGNAL_MAX || this == Int.MIN_VALUE || this == -1 * abs(
                        SIGNAL_UNAVAILABLE
                    )
                ) {
                    null
                } else {
                    -1 * abs(this)
                }
            }
        }

        private fun Int?.fixNrRsrq(): Int? {
            return if (this == null) {
                null
            } else {
                var value = -1 * abs(this)
                if (value < NR_RSRQ_SIGNAL_MIN || value > NR_RSRQ_SIGNAL_MAX || this == Int.MIN_VALUE || this == -1 * abs(
                        SIGNAL_UNAVAILABLE
                    )
                ) {
                    null
                } else {
                    -abs(this)
                }
            }
        }

        private fun Int?.fixNrSinr(): Int? =
            if (this == null || this < NR_SINR_SIGNAL_MIN || this > NR_SINR_SIGNAL_MAX || this == Int.MIN_VALUE || this == SIGNAL_UNAVAILABLE) {
                null
            } else {
                this
            }

        internal fun Int?.checkValueAvailable(): Int? =
            if (this == null || this == Int.MIN_VALUE || this == Int.MAX_VALUE) {
                null
            } else {
                this
            }

        private fun Int?.fixLteTimingAdvance(): Int? =
            if (this == null || this == Int.MIN_VALUE || this == Int.MAX_VALUE || this > 1282 || this < 0) {
                null
            } else {
                this
            }

        private fun Int?.fixGsmTimingAdvance(): Int? =
            if (this == null || this == Int.MIN_VALUE || this == Int.MAX_VALUE || this > 219 || this < 0) {
                null
            } else {
                this
            }

        private fun Int?.fixLteRsrp(): Int? =
            if (this == null || this == Int.MIN_VALUE || this == Int.MAX_VALUE || this < -140 || this == -1) {
                null
            } else {
                this
            }

        private fun Int?.fixLteRsrq(): Int? =
            if (this == null || this == Int.MIN_VALUE || this == Int.MAX_VALUE || abs(this) > 19.5 || abs(this) < 3.0) {
                null
            } else {
                this
            }

        private fun Int?.fixErrorBitRate(): Int? =
            if (this == null || this == Int.MIN_VALUE || this > 99 || this < 0 || (this in 8..98)) {
                null
            } else {
                this
            }
    }
}