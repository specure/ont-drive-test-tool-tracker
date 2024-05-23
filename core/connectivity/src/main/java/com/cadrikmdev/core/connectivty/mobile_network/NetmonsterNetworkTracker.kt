package com.cadrikmdev.core.connectivty.mobile_network

import android.content.Context
import com.cadrikmdev.core.domain.connectivity.NetworkTracker
import cz.mroczis.netmonster.core.INetMonster

class NetmonsterNetworkTracker(
    private val context: Context,
    private val netmonster: INetMonster,
) : NetworkTracker {

}