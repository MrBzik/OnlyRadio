package com.example.radioplayer.data.local.relations

import androidx.room.Entity
import java.sql.Date

@Entity(primaryKeys = ["stationuuid", "date"])
data class StationDateCrossRef (
    val stationuuid : String,
    val date : String
        )