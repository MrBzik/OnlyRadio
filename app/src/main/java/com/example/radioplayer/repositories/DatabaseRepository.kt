package com.example.radioplayer.repositories

import com.example.radioplayer.data.local.RadioDAO
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val radioDAO: RadioDAO
        ) {


    suspend fun insertRadioStation (station : RadioStation) = radioDAO.insertRadioStation(station)

    suspend fun deleteRadioStation (station: RadioStation) = radioDAO.deleteRadioStation(station)

    suspend fun checkIfRadioStationInDB (id : String) = radioDAO.checkIfRadioStationInDB(id)

    suspend fun checkIfStationIsFavoured (stationID: String) = radioDAO.checkIfStationIsFavoured(stationID)

    suspend fun updateIsFavouredState (value : Int, stationID: String)
            = radioDAO.updateIsFavouredState(value, stationID)



    suspend fun insertNewPlaylist (playlist : Playlist) = radioDAO.insertNewPlaylist(playlist)

    suspend fun deletePlaylist (playlistName: String) = radioDAO.deletePlaylist(playlistName)

    suspend fun checkIfInPlaylist (playlistName : String, stationID : String)
            = radioDAO.checkIfInPlaylist(playlistName, stationID)

    suspend fun insertStationPlaylistCrossRef (crossRef: StationPlaylistCrossRef)
            = radioDAO.insertStationPlaylistCrossRef(crossRef)

    suspend fun deleteStationPlaylistCrossRef (crossRef: StationPlaylistCrossRef)
            = radioDAO.deleteStationPlaylistCrossRef(crossRef)

    suspend fun incrementRadioStationPlaylist (stationID : String)
            = radioDAO.incrementRadioStationPlaylist(stationID)

    suspend fun decrementRadioStationPlaylist (stationID : String)
            = radioDAO.decrementRadioStationPlaylist(stationID)

    suspend fun checkIfPlaylistExists (playlistName: String)
            = radioDAO.checkIfPlaylistExists(playlistName)

    fun getAllPlaylists () = radioDAO.getAllPlaylists()



     fun getAllFavouredStations() = radioDAO.getAllFavouredStations()


    suspend fun deleteAllCrossRefOfPlaylist(playlistName: String)
            = radioDAO.deleteAllCrossRefOfPlaylist(playlistName)


    suspend fun testGetAllOneTimePlaylistStations()
            = radioDAO.testGetAllOneTimePlaylistStations()

}