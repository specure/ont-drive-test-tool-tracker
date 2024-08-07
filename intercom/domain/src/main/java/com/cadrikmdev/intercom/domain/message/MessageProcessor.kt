package com.cadrikmdev.intercom.domain.message

import com.cadrikmdev.intercom.domain.data.MeasurementProgress

interface MessageProcessor {
    fun processMessage(message: String?): TrackerAction?

    fun sendMessage(payload: MeasurementProgress?): String?
}