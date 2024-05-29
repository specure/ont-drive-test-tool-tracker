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

package com.cadrikmdev.core.connectivty.domain.connectivity


/**
 * Enum that represents [android.net.NetworkCapabilities] constants for transport types
 */
enum class TransportType(
    /**
     * Value that corresponds to one of [android.net.NetworkCapabilities] TRANSPORT_* constants
     */
    val value: Int
) {
    /**
     * Indicates this network uses a Cellular transport.
     */
    CELLULAR(0),

    /**
     * Indicates this network uses a Wi-Fi transport.
     */
    WIFI(1),

    /**
     * Indicates this network uses a Bluetooth transport.
     */
    BLUETOOTH(2),

    /**
     * Indicates this network uses an Ethernet transport.
     */
    ETHERNET(3),

    /**
     * Indicates this network uses a VPN transport.
     */
    VPN(4),

    /**
     * Indicates this network uses a Wi-Fi Aware transport.
     *
     * android support since Build.VERSION_CODES.O
     */
    WIFI_AWARE(5),

    /**
     * Indicates this network uses a LoWPAN transport.
     *
     * android support since Build.VERSION_CODES.O_MR1
     */
    LOWPAN(6),

    BROWSER(98),

    /**
     * for non-processed network types
     */
    UNKNOWN(-1);
}