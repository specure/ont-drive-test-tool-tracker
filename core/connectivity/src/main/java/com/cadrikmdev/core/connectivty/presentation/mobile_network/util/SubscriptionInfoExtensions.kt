package com.cadrikmdev.core.connectivty.presentation.mobile_network.util

import android.os.Build
import android.telephony.SubscriptionInfo
import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.fixValue

fun SubscriptionInfo.mccCompat(): Int? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mccString?.toInt().fixValue()
    } else {
        mcc.fixValue()
    }

fun SubscriptionInfo.mncCompat(): Int? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mncString?.toInt().fixValue()
    } else {
        mnc.fixValue()
    }
