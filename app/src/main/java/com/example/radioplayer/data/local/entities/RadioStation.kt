package com.example.radioplayer.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "radio_stations")
data class RadioStation (
    @PrimaryKey(autoGenerate = false)
    val stationuuid: String?,

    val favicon: String?,
    val name: String?,
    val country: String?,
    val url: String?
        )