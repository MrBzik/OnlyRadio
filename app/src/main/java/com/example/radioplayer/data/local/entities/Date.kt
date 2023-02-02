package com.example.radioplayer.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HistoryDate (
    @PrimaryKey(autoGenerate = false)
    val date : String,
    val time : Long
        )