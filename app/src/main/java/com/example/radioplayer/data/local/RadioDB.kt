package com.example.radioplayer.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.radioplayer.data.local.entities.*
import com.example.radioplayer.data.local.relations.StationDateCrossRef
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef

@Database (
    entities = [
        RadioStation::class,
        Playlist::class,
        StationPlaylistCrossRef::class,
        HistoryDate::class,
        StationDateCrossRef::class,
        Recording::class,
        Title::class,
        BookmarkedTitle::class
               ],
        version = 19,
         autoMigrations = [
        AutoMigration(from = 9, to = 10, spec = RadioDB.Migration9To10::class),
        AutoMigration(from = 11, to = 12, spec = RadioDB.Migration11To12::class),
        AutoMigration(from = 12, to = 13, spec = RadioDB.Migration12To13::class),
        AutoMigration(from = 13, to = 14, spec = RadioDB.Migration13To14::class),
         AutoMigration(from = 14, to = 15, spec = RadioDB.Migration14To15::class),
         AutoMigration(from = 16, to = 17, spec = RadioDB.Migration16To17::class),
         AutoMigration(from = 18, to = 19, spec = RadioDB.Migration18To19::class)
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

    class Migration13To14 : AutoMigrationSpec

    class Migration14To15 : AutoMigrationSpec

    class Migration16To17 : AutoMigrationSpec

    class Migration18To19 : AutoMigrationSpec

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

        val migration15To16 = object : Migration(15, 16){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS Title (" +
                            "timeStamp INTEGER NOT NULL PRIMARY KEY, " +
                            "date INTEGER NOT NULL," +
                            "title TEXT NOT NULL," +
                            "stationName TEXT NOT NULL," +
                            "stationIconUri TEXT NOT NULL)"
                )
            }
        }

        val migration17To18 = object : Migration(17, 18){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS BookmarkedTitle (" +
                            "timeStamp INTEGER NOT NULL PRIMARY KEY, " +
                            "date INTEGER NOT NULL," +
                            "title TEXT NOT NULL," +
                            "stationName TEXT NOT NULL," +
                            "stationIconUri TEXT NOT NULL)"
                )
            }
        }

    }



}