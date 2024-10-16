package com.specure.core.data.auth

import com.specure.core.domain.AuthInfo

fun AuthInfo.toAuthInfoSerializable(): AuthInfoSerializable {
    return AuthInfoSerializable(
        userId = userId
    )
}

fun AuthInfoSerializable.toAuthInfo(): AuthInfo {
    return AuthInfo(
        userId = userId
    )
}