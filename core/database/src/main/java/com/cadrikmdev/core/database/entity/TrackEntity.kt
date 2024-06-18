package com.cadrikmdev.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cadrikmdev.core.database.Tables
import org.bson.types.ObjectId

@Entity(
    tableName = Tables.TRACK_ENTITY
)
data class TrackEntity(
    val durationMillis: Long,
    val dateTimeUtc: String,
    val latitude: Double,
    val longitude: Double,
    @PrimaryKey(autoGenerate = false)
    val id: String = ObjectId().toHexString()
)
