package com.specure.intercom.domain.message

interface MessageProcessor {
    fun processMessage(message: String?): TrackerAction?

    fun sendAction(action: TrackerAction?): String?
}