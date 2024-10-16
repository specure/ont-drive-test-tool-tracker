package com.specure.intercom.domain.client

import com.specure.core.domain.util.Error

enum class MessagingError : Error {
    CONNECTION_INTERRUPTED,
    DISCONNECTED,
    UNKNOWN
}