package com.specure.core.database

import android.database.sqlite.SQLiteFullException
import com.specure.core.database.dao.TrackDao
import com.specure.core.database.mappers.toTrack
import com.specure.core.database.mappers.toTrackEntity
import com.specure.core.domain.track.LocalTrackDataSource
import com.specure.core.domain.track.Track
import com.specure.core.domain.track.TrackId
import com.specure.core.domain.util.DataError
import com.specure.core.domain.util.Result
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
        return trackDao.getTracksForExport()
            .map { trackEntities ->
                trackEntities.map {
                    it.toTrack()
                }
            }
    }

    override suspend fun getLatestNTracksForExport(limit: Int): Flow<List<Track>> {
        return trackDao.getLastNTracksForExport(limit)
            .map { trackEntities ->
                trackEntities.map {
                    it.toTrack()
                }
            }
    }

    override suspend fun deleteExportedTracks() {
        trackDao.deleteExportedTracks()
    }

    override suspend fun deleteTrack(id: TrackId) {
        trackDao.deleteTrack(id)
    }

    override suspend fun deleteAllTracks() {
        trackDao.deleteAllTracks()
    }
}