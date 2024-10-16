package com.specure.intercom.domain.message

import com.specure.intercom.domain.data.MeasurementProgress

sealed interface Message {
    data object StartMeasurement : Message
    data object StopMeasurement : Message
    data class MeasurementStatus(val progress: MeasurementProgress) : Message
}
