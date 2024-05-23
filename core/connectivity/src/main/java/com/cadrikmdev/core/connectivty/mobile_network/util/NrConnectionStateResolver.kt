package com.cadrikmdev.core.connectivty.mobile_network.util

import android.os.Build
import android.telephony.TelephonyManager
import com.cadrikmdev.core.domain.connectivity.mobile.NrConnectionState

class NrConnectionStateResolver {

    companion object {

        fun getNRConnectionState(telephonyManager: TelephonyManager): NrConnectionState {

            // only for android 9 and up
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                return NrConnectionState.NOT_AVAILABLE
            }

            try {
                val obj = Class.forName(telephonyManager.javaClass.name)
                    .getDeclaredMethod("getServiceState", *arrayOfNulls(0)).invoke(telephonyManager, *arrayOfNulls(0))
                val methods = Class.forName(obj.javaClass.name).declaredMethods

                // try extracting from string
                // source: https://github.com/mroczis/netmonster-core/blob/master/library/src/main/java/cz/mroczis/netmonster/core/feature/detect/DetectorLteAdvancedNrServiceState.kt#L69
                val serviceState = obj.toString()
                val is5gActive = serviceState.contains("nrState=CONNECTED") ||
                        serviceState.contains("nsaState=5") ||
                        serviceState.contains("EnDc=true") &&
                        serviceState.contains("5G Allocated=true")
                if (is5gActive) {
                    return NrConnectionState.NSA
                }
                val is5gAvailable = serviceState.contains("isNrAvailable=true") ||
                        serviceState.contains("isNrAvailable = true")
                if (is5gAvailable) {
                    return NrConnectionState.AVAILABLE
                }
                for (method in methods) {
                    if (method.name == "getNrStatus" || method.name == "getNrState") {
                        method.isAccessible = true
                        if ((method.invoke(obj, *arrayOfNulls(0)) as Int).toInt() == 3) {
                            NrConnectionState.NSA
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return NrConnectionState.NOT_AVAILABLE
        }
    }
}