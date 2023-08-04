package com.onlyradio.radioplayer.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Playlist (

    @PrimaryKey(autoGenerate = false)
    val playlistName : String,
    val coverURI : String
        )