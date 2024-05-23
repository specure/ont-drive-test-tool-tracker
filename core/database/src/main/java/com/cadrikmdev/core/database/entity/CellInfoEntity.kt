package com.cadrikmdev.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cadrikmdev.core.database.Tables
import com.cadrikmdev.core.domain.connectivity.TransportType
import com.cadrikmdev.core.domain.connectivity.mobile.CellTechnology

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
    val transportType: TransportType,
    // another more for mobile cell
    val cellTechnology: CellTechnology?,
    val areaCode: Int?,
    val locationId: Long?,
    val mcc: Int?,
    val mnc: Int?,
    val primaryScramblingCode: Int?,
    val dualSimDetectionMethod: String?,
    val isPrimaryDataSubscription: String?,
    val cellState: String?
)