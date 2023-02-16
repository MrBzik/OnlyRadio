package com.example.radioplayer.data.local.entities

import androidx.room.ColumnInfo
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
    @ColumnInfo(name = "favouredAt", defaultValue = "0")
    val favouredAt : Long
        )

/*  version 9 - 10 :
        val inPlaylists : Int - deleted;
        val isFavoured : Boolean - deleted;
        val favouredAt : Long - added
               */