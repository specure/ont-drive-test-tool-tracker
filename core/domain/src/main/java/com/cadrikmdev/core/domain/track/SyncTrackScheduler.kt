package com.cadrikmdev.core.domain.track

import kotlin.time.Duration

interface SyncTrackScheduler {

    suspend fun scheduleSync(type: SyncType)
    suspend fun cancelAllSyncs()

    sealed interface SyncType {
        data class FetchTracks(val interval: Duration) : SyncType
        data class DeleteTracks(val trackId: TrackId) : SyncType

        class CreateTracks(val track: Track) : SyncType
    }
}