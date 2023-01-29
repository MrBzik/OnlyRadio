package com.example.radioplayer.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class RadioStation (
    @PrimaryKey(autoGenerate = false)
    val stationuuid: String,

    val favicon: String?,
    val name: String?,
    val country: String?,
    val url: String?,
    val homepage : String?,
    val tags : String?,
    val language : String?,
    val inPlaylists : Int,
    val isFavoured : Boolean
        )