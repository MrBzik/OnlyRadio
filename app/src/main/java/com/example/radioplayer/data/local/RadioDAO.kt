package com.example.radioplayer.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.radioplayer.data.local.entities.HistoryDate
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.DateWithStations
import com.example.radioplayer.data.local.relations.PlaylistWithStations
import com.example.radioplayer.data.local.relations.StationDateCrossRef
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef


@Dao
interface  RadioDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRadioStation(station : RadioStation)

    @Query("SELECT * FROM RadioStation WHERE stationuuid =:stationID LIMIT 1")
    suspend fun getCurrentRadioStation(stationID: String) : RadioStation


    // For favoured stations

    @Query("SELECT EXISTS(SELECT * FROM RadioStation WHERE stationuuid =:stationID AND favouredAt > 0)")
    suspend fun checkIfStationIsFavoured(stationID : String) : Boolean

    @Query("SELECT * FROM RadioStation WHERE favouredAt > 0 ORDER BY favouredAt")
    fun getAllFavouredStations() : LiveData<List<RadioStation>>

    @Query("UPDATE RadioStation SET favouredAt =:value WHERE stationuuid =:stationID")
    suspend fun updateIsFavouredState(value : Long, stationID: String)



    // PLAYLISTS

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewPlaylist (playlist : Playlist)

    @Query("DELETE FROM Playlist WHERE playlistName =:playlistName")
    suspend fun deletePlaylist (playlistName: String)

    @Query("SELECT * FROM Playlist")
    fun getAllPlaylists() : LiveData<List<Playlist>>

    @Query("SELECT EXISTS(SELECT * FROM Playlist WHERE playlistName =:playlistName)")
    suspend fun checkIfPlaylistExists (playlistName : String) : Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStationPlaylistCrossRef(stationPlaylistCrossRef: StationPlaylistCrossRef)

    @Delete
    suspend fun deleteStationPlaylistCrossRef(stationPlaylistCrossRef: StationPlaylistCrossRef)

    @Transaction
    @Query("SELECT * FROM Playlist WHERE playlistName =:playlistName")
    suspend fun getStationsInPlaylist(playlistName : String) : List<PlaylistWithStations>


    @Query("DELETE FROM StationPlaylistCrossRef WHERE playlistName =:playlistName")
    suspend fun deleteAllCrossRefOfPlaylist(playlistName: String)


    // For editing playlist

    @Query("UPDATE Playlist SET coverURI =:newCover WHERE playlistName =:playlistName")
    suspend fun editPlaylistCover(playlistName : String, newCover : String)

    @Query("UPDATE Playlist SET playlistName =:newName WHERE playlistName =:oldName")
    suspend fun editPlaylistName(oldName : String, newName : String)

    @Query("UPDATE StationPlaylistCrossRef SET playlistName =:newName WHERE playlistName =:oldName")
    suspend fun editOldCrossRefWithPlaylist(oldName : String, newName : String)


    // Date

    @Query("SELECT EXISTS(SELECT * FROM HistoryDate WHERE date = :currentDate)")
    suspend fun checkLastDateRecordInDB(currentDate : String) : Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewDate(date : HistoryDate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStationDateCrossRef(stationDateCrossRef: StationDateCrossRef)

    // For recyclerView

    @Transaction
    @Query("SELECT * FROM HistoryDate ORDER BY time DESC LIMIT :limit OFFSET :offset")
    suspend fun getStationsInDate(limit: Int, offset : Int) : DateWithStations


    // For database RadioStations cleaning

    @Query("SELECT * FROM RadioStation WHERE favouredAt = 0")
    suspend fun gatherStationsForCleaning() : List<RadioStation>

    @Query("SELECT EXISTS(SELECT * FROM StationPlaylistCrossRef WHERE stationuuid =:stationID)")
    suspend fun checkIfInPlaylists(stationID : String) : Boolean

    @Query("SELECT EXISTS(SELECT * FROM StationDateCrossRef WHERE stationuuid =:stationID)")
    suspend fun checkIfRadioStationInHistory(stationID : String) : Boolean

    @Delete
    suspend fun deleteRadioStation(station : RadioStation)

    // For Dates cleaning

    @Query("SELECT COUNT(date) FROM HistoryDate")
    suspend fun getNumberOfDates() : Int

    @Query("SELECT * FROM HistoryDate ORDER BY time LIMIT :limit")
    suspend fun getDatesToDelete(limit: Int) : List<HistoryDate>

    @Query("DELETE FROM StationDateCrossRef WHERE date =:date")
    suspend fun deleteAllCrossRefWithDate(date : String)

    @Delete
    suspend fun deleteDate(date : HistoryDate)

    //test
    @Query("SELECT COUNT(stationuuid) FROM RadioStation")
    suspend fun getAllStations() : Int

}