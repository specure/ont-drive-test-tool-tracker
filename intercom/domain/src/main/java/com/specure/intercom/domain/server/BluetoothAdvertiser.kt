package com.specure.intercom.domain.server

interface BluetoothAdvertiser {
    fun startAdvertising()

    fun stopAdvertising()
}