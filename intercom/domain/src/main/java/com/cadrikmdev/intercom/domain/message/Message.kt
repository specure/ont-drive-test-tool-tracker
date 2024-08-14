package com.cadrikmdev.intercom.domain.message

import com.cadrikmdev.intercom.domain.data.MeasurementProgress

sealed interface Message {
    data object StartMeasurement : Message
    data object StopMeasurement : Message
    data class MeasurementStatus(val progress: MeasurementProgress) : Message
}
