package com.cadrikmdev.track.network

import com.cadrikmdev.core.domain.track.RemoteTrackDataSource
import com.cadrikmdev.core.domain.track.Track
import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.EmptyResult
import com.cadrikmdev.core.domain.util.Result
import io.ktor.client.HttpClient

class KtorRemoteTrackDataSource(
    private val httpClient: HttpClient
) : RemoteTrackDataSource {
    override suspend fun getTracks(): Result<List<Track>, DataError.Network> {
        return Result.Error<DataError.Network>(DataError.Network.UNKNOWN)
    }

    override suspend fun postTrack(track: Track): Result<Track, DataError.Network> {
        return Result.Error<DataError.Network>(DataError.Network.UNKNOWN)
    }


    override suspend fun deleteTrack(id: String): EmptyResult<DataError.Network> {
        return Result.Error<DataError.Network>(DataError.Network.UNKNOWN)
    }
}