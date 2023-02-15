package com.example.radioplayer.repositories


import androidx.room.Query
import com.example.radioplayer.data.local.RadioDAO
import com.example.radioplayer.data.local.entities.HistoryDate
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.StationDateCrossRef
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import java.sql.Date
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val radioDAO: RadioDAO
        ) {


    suspend fun insertRadioStation (station : RadioStation) = radioDAO.insertRadioStation(station)

    suspend fun checkIfRadioStationInDB (id : String) = radioDAO.checkIfRadioStationInDB(id)

    suspend fun checkIfStationIsFavoured (stationID: String) = radioDAO.checkIfStationIsFavoured(stationID)

    suspend fun updateIsFavouredState (value : Int, stationID: String)
            = radioDAO.updateIsFavouredState(value, stationID)

    suspend fun getCurrentRadioStation(stationID: String) : RadioStation
        = radioDAO.getCurrentRadioStation(stationID)

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


    // For editing playlist


    suspend fun editPlaylistCover(playlistName : String, newCover : String)
        = radioDAO.editPlaylistCover(playlistName, newCover)


    suspend fun editPlaylistName(oldName : String, newName : String)
        = radioDAO.editPlaylistName(oldName, newName)


    suspend fun editOldCrossRefWithPlaylist(oldName : String, newName : String)
        = radioDAO.editOldCrossRefWithPlaylist(oldName, newName)



    // Date

    suspend fun checkLastDateRecordInDB(currentDate : String)
        = radioDAO.checkLastDateRecordInDB(currentDate)

    suspend fun insertNewDate(date : HistoryDate) = radioDAO.insertNewDate(date)

    suspend fun insertStationDateCrossRef(stationDateCrossRef: StationDateCrossRef)
        = radioDAO.insertStationDateCrossRef(stationDateCrossRef)

    // For recyclerView

    suspend fun getStationsInDate(limit : Int, offset : Int)
         = radioDAO.getStationsInDate(limit, offset)




    // Cleaning unused stations


    suspend fun gatherStationsForCleaning() : List<RadioStation>
        = radioDAO.gatherStationsForCleaning()

    suspend fun checkIfRadioStationInHistory(stationID : String) : Boolean
        = radioDAO.checkIfRadioStationInHistory(stationID)

    suspend fun deleteRadioStation (station : RadioStation) = radioDAO.deleteRadioStation(station)


    // Cleaning dates

    suspend fun getNumberOfDates() : Int = radioDAO.getNumberDates()

    suspend fun getDatesToDelete(limit: Int) : List<HistoryDate> = radioDAO.getDatesToDelete(limit)

    suspend fun deleteAllCrossRefWithDate(date : String) = radioDAO.deleteAllCrossRefWithDate(date)

    suspend fun deleteDate(date : HistoryDate) = radioDAO.deleteDate(date)

    suspend fun getAllStations() = radioDAO.getAllStations()

}