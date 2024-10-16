package com.specure.intercom.domain.client

import com.specure.core.domain.util.Error

enum class BluetoothError : Error {
    NO_FINE_LOCATION_PERMISSIONS,
    MISSING_BLUETOOTH_CONNECT_PERMISSION,
    BLUETOOTH_DEVICE_NOT_FOUND,
    UNABLE_TO_CONNECT,
    SERVICE_NOT_FOUND,
    SERVICE_DISCOVERY_FAILED,
    GATT_CONNECTION_FAILED,
    GATT_DISCONNECTED
}