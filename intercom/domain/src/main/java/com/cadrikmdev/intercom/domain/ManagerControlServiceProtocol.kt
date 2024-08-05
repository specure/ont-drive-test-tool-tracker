package com.cadrikmdev.intercom.domain

import java.util.UUID

class ManagerControlServiceProtocol {

    companion object {
        val customServiceUUID: UUID =
            UUID.nameUUIDFromBytes("TrackerManagerControlServiceProtocol".toByteArray())
        val customCharacteristicServiceUUID: UUID =
            UUID.nameUUIDFromBytes("TrackerManagerControlServiceCharacteristicProtocol".toByteArray())
    }

}