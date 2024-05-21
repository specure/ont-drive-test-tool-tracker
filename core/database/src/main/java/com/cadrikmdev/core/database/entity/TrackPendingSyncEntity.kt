package com.cadrikmdev.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackPendingSyncEntity(
    @Embedded val track: TrackEntity,
    @PrimaryKey(autoGenerate = false)
    val trackId: String = track.id,
    val userId: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrackPendingSyncEntity

        if (track != other.track) return false
        if (trackId != other.trackId) return false
        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = track.hashCode()
        result = 31 * result + trackId.hashCode()
        result = 31 * result + userId.hashCode()
        return result
    }
}
