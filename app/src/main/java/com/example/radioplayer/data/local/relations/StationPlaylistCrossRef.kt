package com.example.radioplayer.data.local.relations

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["stationuuid", "playlistName"])
data class StationPlaylistCrossRef (
    val stationuuid : String,
    val playlistName : String,
    @ColumnInfo(name = "addedAt", defaultValue = "0")
    val addedAt : Long
        )