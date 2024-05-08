package com.cadrikmdev.auth.domain

import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.EmptyResult

interface AuthRepository {

    suspend fun login(email: String, password: String): EmptyResult<DataError.Network>
    suspend fun register(email: String, password: String): EmptyResult<DataError.Network>
}