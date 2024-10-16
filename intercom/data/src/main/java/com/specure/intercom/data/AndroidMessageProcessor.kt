package com.specure.intercom.data

import com.specure.intercom.data.message.TrackerActionDto
import com.specure.intercom.data.message.toTrackerAction
import com.specure.intercom.data.message.toTrackerActionDto
import com.specure.intercom.domain.message.MessageProcessor
import com.specure.intercom.domain.message.TrackerAction
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class AndroidMessageProcessor(

) : MessageProcessor {

    val json = Json {
        serializersModule = SerializersModule {
            polymorphic(TrackerActionDto::class) {
                subclass(TrackerActionDto.StartTest::class)
                subclass(TrackerActionDto.StopTest::class)
                subclass(TrackerActionDto.UpdateProgress::class)
            }
        }
        classDiscriminator = "type"
        encodeDefaults = true
    }

    override fun processMessage(message: String?): TrackerAction? {
        try {
            message?.let { mess ->
                return json.decodeFromString<TrackerActionDto>(mess).toTrackerAction()
            }
        } catch (e: SerializationException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return null
    }

    override fun sendAction(action: TrackerAction?): String? {
        try {
            action?.let {
                return json.encodeToString(it.toTrackerActionDto()) + "\n"
            }
        } catch (e: SerializationException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return null
    }
}