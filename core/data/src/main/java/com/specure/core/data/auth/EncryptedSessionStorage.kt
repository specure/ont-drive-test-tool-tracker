package com.specure.core.data.auth

import android.content.SharedPreferences
import com.specure.core.domain.AuthInfo
import com.specure.core.domain.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EncryptedSessionStorage(
    private val sharedPreferences: SharedPreferences
) : SessionStorage {
    override suspend fun get(): AuthInfo? {
        return withContext(Dispatchers.IO) {
            val authInfoJson = sharedPreferences.getString(KEY_AUTH_INFO, null)
            authInfoJson?.let {
                Json.decodeFromString<AuthInfoSerializable>(it).toAuthInfo()
            }
        }
    }

    override suspend fun set(info: AuthInfo?) {
        withContext(Dispatchers.IO) {
            if (info == null) {
                sharedPreferences.edit().remove(KEY_AUTH_INFO).commit()
                return@withContext
            }
            val authInfoJson = Json.encodeToString(info.toAuthInfoSerializable())

            sharedPreferences
                .edit()
                .putString(KEY_AUTH_INFO, authInfoJson)
                .commit()
        }
    }

    companion object {
        private const val KEY_AUTH_INFO = "KEY_AUTH_INFO"
    }
}