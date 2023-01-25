package com.example.radioplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.radioplayer.data.local.entities.RadioStation

@Database (entities = [RadioStation::class],
        version = 3)
abstract class RadioDB : RoomDatabase() {

    abstract fun getRadioDAO() : RadioDAO

}