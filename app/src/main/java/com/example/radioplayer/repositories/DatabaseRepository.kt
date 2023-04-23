package com.example.radioplayer.repositories


import com.example.radioplayer.data.local.RadioDAO
import com.example.radioplayer.data.local.entities.*
import com.example.radioplayer.data.local.relations.StationDateCrossRef
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val radioDAO: RadioDAO
        ) {


    suspend fun insertRadioStation (station : RadioStation) = radioDAO.insertRadioStation(station)

    suspend fun checkIfStationIsFavoured (stationID: String) = radioDAO.checkIfStationIsFavoured(stationID)

    suspend fun updateIsFavouredState (value : Long, stationID: String)
            = radioDAO.updateIsFavouredState(value, stationID)

    suspend fun getCurrentRadioStation(stationID: String) : RadioStation
        = radioDAO.getCurrentRadioStation(stationID)

    suspend fun insertNewPlaylist (playlist : Playlist) = radioDAO.insertNewPlaylist(playlist)

    suspend fun deletePlaylist (playlistName: String) = radioDAO.deletePlaylist(playlistName)

    suspend fun checkIfInPlaylists (stationID : String)
            = radioDAO.checkIfInPlaylists(stationID)

    suspend fun insertStationPlaylistCrossRef (crossRef: StationPlaylistCrossRef)
            = radioDAO.insertStationPlaylistCrossRef(crossRef)

    suspend fun getTimeOfStationPlaylistInsertion(stationID : String, playlistName : String)
            = radioDAO.getTimeOfStationPlaylistInsertion(stationID, playlistName)

    suspend fun deleteStationPlaylistCrossRef (stationID: String, playlistName: String)
            = radioDAO.deleteStationPlaylistCrossRef(stationID, playlistName)


    fun getAllPlaylists () = radioDAO.getAllPlaylists()



    fun getAllFavouredStations() = radioDAO.getAllFavouredStations()


    fun subscribeToStationsInPlaylist(playlistName: String) = radioDAO.subscribeToStationsInPlaylist(playlistName)

   suspend fun getPlaylistOrder(playlistName : String) = radioDAO.subscribeToPlaylistOrder(playlistName)

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

    suspend fun getLastDate() = radioDAO.getLastDate()

    val getListOfDates = radioDAO.getListOfDates()

    // For recyclerView

    suspend fun getStationsInDate(limit : Int, offset : Int)
         = radioDAO.getStationsInAllDates(limit, offset)




    // Cleaning unused stations


    suspend fun gatherStationsForCleaning() : List<RadioStation>
        = radioDAO.gatherStationsForCleaning()

    suspend fun checkIfRadioStationInHistory(stationID : String) : Boolean
        = radioDAO.checkIfRadioStationInHistory(stationID)

    suspend fun deleteRadioStation (station : RadioStation) = radioDAO.deleteRadioStation(station)


    // Cleaning dates

    suspend fun getNumberOfDates() : Int = radioDAO.getNumberOfDates()

    suspend fun getDatesToDelete(limit: Int) : List<HistoryDate> = radioDAO.getDatesToDelete(limit)

    suspend fun deleteAllCrossRefWithDate(date : String) = radioDAO.deleteAllCrossRefWithDate(date)

    suspend fun deleteDate(date : HistoryDate) = radioDAO.deleteDate(date)

    suspend fun getAllStations() = radioDAO.getAllStations()


    // Title

    suspend fun getTitlesPage(offset: Int, limit: Int) = radioDAO.getTitlesPage(offset, limit)

    suspend fun getTitlesInOneDatePage(offset: Int, limit: Int, date: Long) =
        radioDAO.getTitlesInOneDatePage(offset, limit, date)

    suspend fun deleteTitlesWithDate(time : Long) = radioDAO.deleteTitlesWithDate(time)

    // Recording


    suspend fun getCurrentRecording(id : String)  = radioDAO.getCurrentRecording(id)

    suspend fun renameRecording(id : String, newName: String) = radioDAO.renameRecording(id, newName)

    suspend fun insertRecording(recording : Recording) = radioDAO.insertRecording(recording)

    suspend fun deleteRecording(recId : String) = radioDAO.deleteRecording(recId)

}