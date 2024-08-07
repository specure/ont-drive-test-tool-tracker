package com.cadrikmdev.intercom.data

import com.cadrikmdev.intercom.data.message.TrackerActionDto
import com.cadrikmdev.intercom.data.message.toTrackerAction
import com.cadrikmdev.intercom.domain.data.MeasurementProgress
import com.cadrikmdev.intercom.domain.message.MessageProcessor
import com.cadrikmdev.intercom.domain.message.TrackerAction
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidMessageProcessor(

) : MessageProcessor {

    override fun processMessage(message: String?): TrackerAction? {
        try {
            message?.let { mess ->
                return Json.decodeFromString<TrackerActionDto>(mess).toTrackerAction()
            }
        } catch (e: SerializationException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return null
    }

    override fun sendMessage(payload: MeasurementProgress?): String? {
        try {
            payload?.let {
                return Json.encodeToString(it) + "\n"
            }
        } catch (e: SerializationException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return null
    }
}