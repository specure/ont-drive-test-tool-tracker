package com.cadrikmdev.core.connectivty.presentation.mobile_network

import android.content.Context
import com.cadrikmdev.core.connectivty.domain.connectivity.NetworkTracker
import cz.mroczis.netmonster.core.INetMonster

class NetmonsterNetworkTracker(
    private val context: Context,
    private val netmonster: INetMonster,
) : com.cadrikmdev.core.connectivty.domain.connectivity.NetworkTracker {

}