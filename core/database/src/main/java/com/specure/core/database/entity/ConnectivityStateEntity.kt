package com.specure.core.database.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.specure.core.database.Tables

@Keep
@Entity(tableName = Tables.CONNECTIVITY_STATE)
data class ConnectivityStateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val uuid: String,

    val state: String,

    val message: String?,

    val timeNanos: Long
)