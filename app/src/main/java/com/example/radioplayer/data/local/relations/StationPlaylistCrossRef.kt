package com.example.radioplayer.data.local.relations

import androidx.room.Entity

@Entity(primaryKeys = ["stationuuid", "playlistName"])
data class StationPlaylistCrossRef (
    val stationuuid : String,
    val playlistName : String
        )