package com.example.radioplayer.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.example.radioplayer.data.local.entities.HistoryDate
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.StationDateCrossRef
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef

@Database (
    entities = [
        RadioStation::class,
        Playlist::class,
        StationPlaylistCrossRef::class,
        HistoryDate::class,
        StationDateCrossRef::class
               ],
        version = 10,
         autoMigrations = [
        AutoMigration(from = 9, to = 10, spec = RadioDB.Migration9To10::class)
    ]
)

abstract class RadioDB : RoomDatabase() {

    abstract fun getRadioDAO() : RadioDAO

    @DeleteColumn.Entries(
        DeleteColumn(tableName = "RadioStation", columnName = "inPlaylists"),
        DeleteColumn(tableName = "RadioStation", columnName = "isFavoured"),
    )
    class Migration9To10 : AutoMigrationSpec

}