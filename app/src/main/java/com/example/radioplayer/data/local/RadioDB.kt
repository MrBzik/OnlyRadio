package com.example.radioplayer.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.radioplayer.data.local.entities.HistoryDate
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.data.local.relations.StationDateCrossRef
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef

@Database (
    entities = [
        RadioStation::class,
        Playlist::class,
        StationPlaylistCrossRef::class,
        HistoryDate::class,
        StationDateCrossRef::class,
        Recording::class
               ],
        version = 13,
         autoMigrations = [
        AutoMigration(from = 9, to = 10, spec = RadioDB.Migration9To10::class),
        AutoMigration(from = 11, to = 12, spec = RadioDB.Migration11To12::class),
        AutoMigration(from = 12, to = 13, spec = RadioDB.Migration12To13::class)
    ]
)

abstract class RadioDB : RoomDatabase() {

    abstract fun getRadioDAO() : RadioDAO

    @DeleteColumn.Entries(
        DeleteColumn(tableName = "RadioStation", columnName = "inPlaylists"),
        DeleteColumn(tableName = "RadioStation", columnName = "isFavoured"),
    )
    class Migration9To10 : AutoMigrationSpec

    class Migration11To12 : AutoMigrationSpec

    @DeleteColumn(tableName = "Recording", columnName = "duration")
    class Migration12To13 : AutoMigrationSpec

    companion object{

        val migration10To11 = object : Migration(10, 11){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS Recording (" +
                            "id TEXT NOT NULL PRIMARY KEY," +
                            "iconUri TEXT NOT NULL," +
                            "timeStamp INTEGER NOT NULL," +
                            "name TEXT NOT NULL)"
                )
            }
        }
    }



}