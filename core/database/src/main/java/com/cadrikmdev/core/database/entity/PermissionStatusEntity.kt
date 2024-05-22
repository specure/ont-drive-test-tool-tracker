package com.cadrikmdev.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cadrikmdev.core.database.Tables

@Entity(
    tableName = Tables.PERMISSIONS_STATUS
)
data class PermissionStatusEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testUUID: String?,
    val signalChunkId: String?,
    val permissionName: String,
    val status: Boolean
)