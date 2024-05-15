package com.cadrikmdev.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeleteRunSyncEntity(
    @PrimaryKey(autoGenerate = false)
    val runId: String,
    val userId: String,
)