package com.cadrikmdev.auth.data

import com.cadrikmdev.auth.domain.AuthRepository
import com.cadrikmdev.core.data.networking.post
import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.EmptyResult
import io.ktor.client.HttpClient

class AuthRepositoryImpl(
    private val httpClient: HttpClient
) : AuthRepository {
    override suspend fun register(email: String, password: String): EmptyResult<DataError.Network> {
        return httpClient.post<RegisterRequest, Unit>(
            route = "/register",
            body = RegisterRequest(
                email,
                password
            )
        )
    }

}