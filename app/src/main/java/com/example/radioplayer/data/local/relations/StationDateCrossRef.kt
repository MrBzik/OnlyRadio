package com.example.radioplayer.data.local.relations

import androidx.room.Entity

@Entity(primaryKeys = ["stationuuid", "date"])
data class StationDateCrossRef (
    val stationuuid : String,
    val date : String
        )