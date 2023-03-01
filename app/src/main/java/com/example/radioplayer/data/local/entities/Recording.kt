package com.example.radioplayer.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Recording (

    @PrimaryKey(autoGenerate = false)
    val id : String,
    val iconUri : String,
    val timeStamp : Long,
    val name : String,
    @ColumnInfo(name = "duration", defaultValue = "unknown")
    val duration : String
        )