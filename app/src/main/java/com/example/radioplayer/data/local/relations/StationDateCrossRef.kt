package com.example.radioplayer.data.local.relations

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.sql.Date

@Entity(primaryKeys = ["stationuuid", "date"])
data class StationDateCrossRef (
    val stationuuid : String,
    @ColumnInfo(index = true)
    val date : String
        )