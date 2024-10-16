package com.specure.core.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthInfoSerializable(
    val userId: String
)