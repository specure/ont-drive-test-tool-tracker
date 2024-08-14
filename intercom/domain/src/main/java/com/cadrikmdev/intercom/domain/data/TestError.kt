package com.cadrikmdev.intercom.domain.data

import com.cadrikmdev.core.domain.util.Error

enum class TestError : Error {
    DOWNLOAD_TEST_ERROR,
    UPLOAD_TEST_ERROR,
    UNKNOWN_ERROR,
}