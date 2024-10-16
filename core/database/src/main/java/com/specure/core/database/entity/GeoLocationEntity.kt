package com.specure.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.specure.core.database.Tables

@Entity(
    tableName = Tables.GEO_LOCATION
)
data class GeoLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testUUID: String?,
    val signalChunkId: String?,
    val latitude: Double,
    val longitude: Double,
    val provider: String,
    val speed: Float,
    val altitude: Double,
    /**
     * time from [android.location.Location] object, timestamp of acquired position
     */
    val timestampMillis: Long,
    /**
     * relative time from the start of the test in nanoseconds
     */
    val timeRelativeNanos: Long,
    val ageNanos: Long,
    val accuracy: Float,
    val bearing: Float,
    val isMocked: Boolean,
    val satellitesCount: Int
)