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


/**
 * Type of Mobile network technology according to [android.telephony.TelephonyManager].NETWORK_TYPE_*
 */
enum class MobileNetworkType(val intValue: Int, val displayName: String) {

    /**
     *  Network type is unknown
     */
    UNKNOWN(0, "MOBILE"),

    /**
     * Current network is GPRS
     */
    GPRS(1, "GPRS"),

    /**
     * Current network is EDGE
     */
    EDGE(2, "EDGE"),

    /**
     * Current network is UMTS
     */
    UMTS(3, "UMTS"),

    /**
     * Current network is CDMA: Either IS95A or IS95B
     */
    CDMA(4, "CDMA"),

    /**
     * Current network is EVDO revision 0
     */
    EVDO_0(5, "EVDO_0"),

    /**
     *  Current network is EVDO revision A
     */
    EVDO_A(6, "EVDO_A"),

    /**
     * Current network is 1xRTT
     */
    _1xRTT(7, "1xRTT"),

    /**
     * Current network is HSDPA
     */
    HSDPA(8, "HSDPA"),

    /**
     *  Current network is HSUPA
     */
    HSUPA(9, "HSUPA"),

    /**
     * Current network is HSPA
     */
    HSPA(10, "HSPA"),

    /**
     * Current network is iDen
     */
    IDEN(11, "IDEN"),

    /**
     * Current network is EVDO revision B
     */
    EVDO_B(12, "EVDO_B"),

    /**
     * Current network is LTE
     */
    LTE(13, "LTE"),

    /**
     * Current network is eHRPD
     */
    EHRPD(14, "EHRPD"),

    /**
     * Current network is HSPA+
     */
    HSPAP(15, "HSPA+"),

    /**
     *  Current network is GSM
     */
    GSM(16, "GSM"),

    /**
     *  Current network is TD_SCDMA
     */
    TD_SCDMA(17, "TD-SCDMA"),

    /**
     * Current network is IWLAN
     */
    IWLAN(18, "IWLAN"),

    /**
     * Current network is LTE_CA {@hide}
     */
    LTE_CA(19, "LTE CA"),

    /**
     *  Current network is NR(New Radio) 5G.
     */
    NR_SA(20, "NR"),

    /**
     * Current network is NR(New Radio) Non standalone mode 5G.
     */
    NR_NSA(41, "NR NSA"),

    /**
     * Current network is LTE with 5G signalling
     */
    NR_AVAILABLE(40, "LTE+(NR)");

    companion object {

        fun fromValue(intValue: Int): MobileNetworkType {
            for (value in values()) {
                if (value.intValue == intValue) {
                    // fix because of netmonster library returns LTE_CA in cases when it is plain LTE - so we decided to report only LTE in case LTE and LTE_CA
                    return if (value == LTE_CA)
                        LTE
                    else
                        value
                }
            }
            return UNKNOWN
        }
    }
}