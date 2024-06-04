package com.cadrikmdev.core.connectivty.presentation.mobile_network.util

import android.os.Build
import android.telephony.CellIdentityCdma
import android.telephony.CellIdentityGsm
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellIdentityTdscdma
import android.telephony.CellIdentityWcdma
import androidx.annotation.RequiresApi
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.Q)
private fun CellIdentityNr.uuid(): String {
    val id = buildString {
        append("nr")
        append(nci)
        append(pci)
    }.toByteArray()
    return UUID.nameUUIDFromBytes(id).toString()
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun CellIdentityTdscdma.uuid(): String {
    val id = buildString {
        append("tdscdma")
        append(cid)
        append(cpid)
    }.toByteArray()
    return UUID.nameUUIDFromBytes(id).toString()
}

private fun CellIdentityLte.uuid(): String {
    val id = buildString {
        append("lte")
        append(ci)
        append(pci)
    }.toByteArray()
    return UUID.nameUUIDFromBytes(id).toString()
}

private fun CellIdentityWcdma.uuid(): String {
    val id = buildString {
        append("wcdma")
        append(cid)
    }.toByteArray()
    return UUID.nameUUIDFromBytes(id).toString()
}

private fun CellIdentityGsm.uuid(): String {
    val id = buildString {
        append("gsm")
        append(cid)
    }.toByteArray()
    return UUID.nameUUIDFromBytes(id).toString()
}

private fun CellIdentityCdma.uuid(): String {
    val id = buildString {
        append("cdma")
        append(networkId)
        append(systemId)
    }.toByteArray()
    return UUID.nameUUIDFromBytes(id).toString()
}