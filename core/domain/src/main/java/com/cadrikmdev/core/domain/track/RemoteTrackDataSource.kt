package com.cadrikmdev.core.domain.track

import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.EmptyResult
import com.cadrikmdev.core.domain.util.Result

interface RemoteTrackDataSource {
    suspend fun getTracks(): Result<List<Track>, DataError.Network>

    suspend fun postTrack(track: Track): Result<Track, DataError.Network>

    suspend fun deleteTrack(id: String): EmptyResult<DataError.Network>
}