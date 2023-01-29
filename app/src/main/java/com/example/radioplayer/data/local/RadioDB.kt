package com.example.radioplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef

@Database (
    entities = [
        RadioStation::class,
        Playlist::class,
        StationPlaylistCrossRef::class
               ],
        version = 5
)
abstract class RadioDB : RoomDatabase() {

    abstract fun getRadioDAO() : RadioDAO

}