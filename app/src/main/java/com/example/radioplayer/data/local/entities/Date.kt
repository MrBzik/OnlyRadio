package com.example.radioplayer.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Date (
    @PrimaryKey(autoGenerate = false)
    val date : String,

    val dateInMills : Long
        )