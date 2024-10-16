package com.specure.core.domain.track

import com.specure.core.domain.util.DataError
import com.specure.core.domain.util.EmptyResult
import com.specure.core.domain.util.Result

interface RemoteTrackDataSource {
    suspend fun getTracks(): Result<List<Track>, DataError.Network>

    suspend fun postTrack(track: Track): Result<Track, DataError.Network>

    suspend fun deleteTrack(id: String): EmptyResult<DataError.Network>
}