package com.specure.intercom.domain.data

import com.specure.core.domain.util.Error

enum class TestError : Error {
    DOWNLOAD_TEST_ERROR,
    UPLOAD_TEST_ERROR,
    UNKNOWN_ERROR,
}