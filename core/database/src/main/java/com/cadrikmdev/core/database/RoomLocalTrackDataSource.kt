package com.cadrikmdev.core.database

import android.database.sqlite.SQLiteFullException
import com.cadrikmdev.core.database.dao.TrackDao
import com.cadrikmdev.core.database.mappers.toTrack
import com.cadrikmdev.core.database.mappers.toTrackEntity
import com.cadrikmdev.core.domain.track.LocalTrackDataSource
import com.cadrikmdev.core.domain.track.Track
import com.cadrikmdev.core.domain.track.TrackId
import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLocalTrackDataSource(
    private val trackDao: TrackDao
) : LocalTrackDataSource {
    override fun getTracks(): Flow<List<Track>> {
        return trackDao.getTracks()
            .map { trackEntities ->
                trackEntities.map {
                    it.toTrack()
                }
            }
    }

    override suspend fun upsertTrack(track: Track): Result<TrackId, DataError.Local> {
        return try {
            val entity = track.toTrackEntity()
            trackDao.upsertTrack(entity)
            Result.Success(entity.id)
        } catch (e: SQLiteFullException) {
            Result.Error(DataError.Local.DISK_FULL)
        }
    }

    override suspend fun upsertTracks(tracks: List<Track>): Result<List<TrackId>, DataError.Local> {
        return try {
            val entities = tracks.map {
                it.toTrackEntity()
            }
            trackDao.upsertTracks(entities)
            Result.Success(entities.map { it.id })
        } catch (e: SQLiteFullException) {
            Result.Error(DataError.Local.DISK_FULL)
        }
    }

    override suspend fun getTracksForExport(): Flow<List<Track>> {
        TODO("Not yet implemented")
    }

    override suspend fun getLatestNTracksForExport(limit: Int): Flow<List<Track>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteExportedTracks() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTrack(id: TrackId) {
        trackDao.deleteTrack(id)
    }

    override suspend fun deleteAllTracks() {
        trackDao.deleteAllTracks()
    }
}