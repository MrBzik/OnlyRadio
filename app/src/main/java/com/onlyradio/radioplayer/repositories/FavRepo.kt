package com.onlyradio.radioplayer.repositories



import com.onlyradio.radioplayer.data.local.RadioDAO
import com.onlyradio.radioplayer.data.local.entities.Playlist
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.data.local.relations.StationPlaylistCrossRef
import javax.inject.Inject

class FavRepo @Inject constructor(
    private val radioDAO: RadioDAO
        ) {

    suspend fun insertRadioStation (station : RadioStation) = radioDAO.insertRadioStation(station)

    suspend fun checkIfStationIsFavoured (stationID: String) = radioDAO.checkIfStationIsFavoured(stationID)

    suspend fun updateIsFavouredState (value : Long, stationID: String)
            = radioDAO.updateIsFavouredState(value, stationID)

    suspend fun insertNewPlaylist (playlist : Playlist) = radioDAO.insertNewPlaylist(playlist)

    suspend fun deletePlaylist (playlistName: String) = radioDAO.deletePlaylist(playlistName)


    // Adding / removing station in / from playlist

    suspend fun insertStationPlaylistCrossRef (crossRef: StationPlaylistCrossRef)
            = radioDAO.insertStationPlaylistCrossRef(crossRef)

    suspend fun incrementInPlaylistsCount(stationID: String) = radioDAO.incrementInPlaylistsCount(stationID)

    suspend fun decrementInPlaylistsCount(stationID: String) = radioDAO.decrementInPlaylistsCount(stationID)


    suspend fun getStationsIdsFromPlaylist(playlistName: String) = radioDAO.getStationsIdsFromPlaylist(playlistName)

    suspend fun deleteStationPlaylistCrossRef (stationID: String, playlistName: String)
            = radioDAO.deleteStationPlaylistCrossRef(stationID, playlistName)

    suspend fun getTimeOfStationPlaylistInsertion(stationID : String, playlistName : String)
            = radioDAO.getTimeOfStationPlaylistInsertion(stationID, playlistName)

    suspend fun checkIfAlreadyInPlaylist(stationID : String, playlistName : String)
            = radioDAO.checkIfAlreadyInPlaylist(stationID, playlistName)


    fun getAllPlaylists () = radioDAO.getAllPlaylists()

    fun getAllFavStationsFlow () = radioDAO.getAllFavStationsDistinct()


    fun getStationsInPlaylistFlow(playlistName: String) = radioDAO.getStationsInPlaylistFlowDistinct(playlistName)

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



}