package com.cadrikmdev.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeleteTrackSyncEntity(
    @PrimaryKey(autoGenerate = false)
    val trackId: String,
    val userId: String,
)