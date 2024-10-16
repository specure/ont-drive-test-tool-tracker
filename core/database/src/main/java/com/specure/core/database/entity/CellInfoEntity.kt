package com.specure.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.specure.core.database.Tables

@Entity(
    tableName = Tables.CELL_INFO
)
data class CellInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testUUID: String?,
    val signalChunkId: String?,
    val isActive: Boolean,
    val uuid: String,
    val channelNumber: Int?,
    val frequency: Double?,
    val registered: Boolean,
    val transportType: Int,
    // another more for mobile cell
    val cellTechnology: String?,
    val areaCode: Int?,
    val locationId: Long?,
    val mcc: Int?,
    val mnc: Int?,
    val primaryScramblingCode: Int?,
    val dualSimDetectionMethod: String?,
    val isPrimaryDataSubscription: String?,
    val cellState: String?
)