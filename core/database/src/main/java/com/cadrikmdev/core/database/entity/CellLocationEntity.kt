package com.cadrikmdev.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cadrikmdev.core.database.Tables

@Entity(
    tableName = Tables.CELL_LOCATION
)
data class CellLocationEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testUUID: String?,
    val signalChunkId: String?,
    val scramblingCode: Int,
    val areaCode: Int?,
    val locationId: Long?,
    val timestampMillis: Long,
    val timestampNanos: Long
)