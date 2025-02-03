package com.specure.signaltracker.intercom

import com.cadrikmdev.intercom.domain.BluetoothServiceSpecification
import java.util.UUID

class AppBluetoothServiceSpecification : BluetoothServiceSpecification {

    override fun getBluetoothServiceName(): String {
        return "TrackerManagerControlServiceProtocol"
    }

    override fun getServiceCharacteristicUUID(): UUID {
        return UUID.nameUUIDFromBytes("TrackerManagerControlServiceCharacteristicProtocol".toByteArray())
    }

}