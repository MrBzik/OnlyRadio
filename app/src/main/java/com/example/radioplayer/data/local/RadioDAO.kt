package com.example.radioplayer.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.PlaylistWithStations
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef

@Dao
interface  RadioDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRadioStation(station : RadioStation)

    @Delete
    suspend fun deleteRadioStation(station : RadioStation)

    @Query("SELECT * FROM RadioStation")
    suspend fun getAllStations() : List<RadioStation>

    @Query("SELECT * FROM RadioStation")
    fun  getAllStationsTEST() : LiveData<List<RadioStation>>

    @Query("SELECT EXISTS(SELECT * FROM RadioStation WHERE stationuuid =:id) ")
    suspend fun checkIfRadioStationInDB (id : String) : Boolean

    @Query("SELECT EXISTS(SELECT * FROM RadioStation WHERE stationuuid =:stationID AND isFavoured = 1)")
    suspend fun checkIfStationIsFavoured(stationID : String) : Boolean

    @Query("UPDATE RadioStation SET isFavoured =:value WHERE stationuuid =:stationID")
    suspend fun updateIsFavouredState(value : Int, stationID: String)




    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewPlaylist (playlist : Playlist)

    @Delete
    suspend fun deletePlaylist (playlist: Playlist)

    @Query("SELECT * FROM Playlist")
    fun getAllPlaylists() : LiveData<List<Playlist>>

    @Query("SELECT EXISTS(SELECT * FROM Playlist WHERE playlistName =:playlistName)")
    suspend fun checkIfPlaylistExists (playlistName : String) : Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStationPlaylistCrossRef(stationPlaylistCrossRef: StationPlaylistCrossRef)

    @Delete
    suspend fun deleteStationPlaylistCrossRef(stationPlaylistCrossRef: StationPlaylistCrossRef)

    @Query("SELECT EXISTS(SELECT * FROM StationPlaylistCrossRef WHERE playlistName =:playlistName AND stationuuid =:stationID)")
    suspend fun checkIfInPlaylist(playlistName : String, stationID : String) : Boolean

    @Query("UPDATE RadioStation SET inPlaylists = inPlaylists+1 WHERE stationuuid =:stationID")
    suspend fun incrementRadioStationPlaylist(stationID : String)

    @Query("UPDATE RadioStation SET inPlaylists = inPlaylists-1 WHERE stationuuid =:stationID")
    suspend fun decrementRadioStationPlaylist(stationID : String)



    @Transaction
    @Query("SELECT * FROM Playlist WHERE playlistName =:playlistName")
    suspend fun getStationsInPlaylist(playlistName : String) : List<PlaylistWithStations>

    @Query("SELECT * FROM RadioStation WHERE isFavoured = 1")
    fun getAllFavouredStations() : LiveData<List<RadioStation>>


    @Query("DELETE FROM StationPlaylistCrossRef WHERE playlistName =:playlistName")
    suspend fun deleteAllCrossRefOfPlaylist(playlistName: String)


    @Query("SELECT * FROM RadioStation WHERE inPlaylists = 1")
    suspend fun testGetAllOneTimePlaylistStations() : List<RadioStation>
}