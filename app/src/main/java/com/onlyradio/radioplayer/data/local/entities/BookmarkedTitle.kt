package com.onlyradio.radioplayer.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookmarkedTitle(
    @PrimaryKey(autoGenerate = false)
    val timeStamp : Long,
    val date : Long,
    val title : String,
    val stationName : String,
    val stationIconUri : String
)
