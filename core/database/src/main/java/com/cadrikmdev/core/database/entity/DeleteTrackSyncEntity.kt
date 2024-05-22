package com.cadrikmdev.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cadrikmdev.core.database.Tables

@Entity(
    tableName = Tables.DELETE_TRACK_SYNC_ENTITY
)
data class DeleteTrackSyncEntity(
    @PrimaryKey(autoGenerate = false)
    val trackId: String,
    val userId: String,
)