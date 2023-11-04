package com.onlyradio.radioplayer.ui.viewmodels

import androidx.core.os.bundleOf
import androidx.lifecycle.*
import androidx.paging.*
import com.onlyradio.radioplayer.adapters.datasources.*
import com.onlyradio.radioplayer.data.local.entities.*
import com.onlyradio.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.exoPlayer.RadioServiceConnection
import com.onlyradio.radioplayer.exoPlayer.RadioSource
import com.onlyradio.radioplayer.repositories.FavRepo
import com.onlyradio.radioplayer.utils.Commands.COMMAND_CLEAR_MEDIA_ITEMS
import com.onlyradio.radioplayer.utils.Commands.COMMAND_ON_DROP_STATION_IN_PLAYLIST
import com.onlyradio.radioplayer.utils.Commands.COMMAND_ON_SWIPE_DELETE
import com.onlyradio.radioplayer.utils.Commands.COMMAND_ON_SWIPE_RESTORE
import com.onlyradio.radioplayer.utils.Commands.COMMAND_REMOVE_MEDIA_ITEM
import com.onlyradio.radioplayer.utils.Commands.COMMAND_UPDATE_FAV_PLAYLIST
import com.onlyradio.radioplayer.utils.Constants
import com.onlyradio.radioplayer.utils.Constants.ITEM_ID
import com.onlyradio.radioplayer.utils.Constants.ITEM_PLAYLIST
import com.onlyradio.radioplayer.utils.Constants.ITEM_PLAYLIST_NAME
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_LAZY_LIST
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.onlyradio.radioplayer.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DatabaseViewModel @Inject constructor(
    private val repository: FavRepo,
    private val radioSource: RadioSource,
    private val radioServiceConnection: RadioServiceConnection
) : ViewModel() {



    val isStationFavoured: MutableLiveData<Boolean> = MutableLiveData()

    var currentPlaylistName: MutableLiveData<String> = MutableLiveData("")


    fun isToChangeMediaItems() : Boolean {

        var isToChangeMediaItems = false

        // When click on station from playlist and before that was another playlist or this is the first playlist
        if(currentPlaylistName.value != RadioService.currentPlaylistName && favFragStationsSwitch.value == SEARCH_FROM_PLAYLIST) {

            RadioSource.updatePlaylistStations()

            RadioService.currentPlaylistName = currentPlaylistName.value ?: ""
            isToChangeMediaItems = true
        }

        // When flag changed
        else if(favFragStationsSwitch.value != RadioService.currentMediaItems){
            isToChangeMediaItems = true

        }

        return isToChangeMediaItems

    }


    fun checkIfStationIsFavoured(stationID: String) = viewModelScope.launch {
        val check = repository.checkIfStationIsFavoured(stationID)
        isStationFavoured.postValue(check)
    }

    fun updateIsFavouredState(value: Long, stationID: String) = viewModelScope.launch {
        repository.updateIsFavouredState(value, stationID)
    }


    fun insertNewPlayList(playlist: Playlist) = viewModelScope.launch {
        repository.insertNewPlaylist(playlist)
    }


    fun checkAndInsertStationPlaylistCrossRef(stationID: String, playlistName: String,
                    resultHandler : (Boolean) -> Unit)
        = viewModelScope.launch {
        handleCheckAndInsertStationInPlaylist(stationID, playlistName, resultHandler)
    }

    private suspend fun handleCheckAndInsertStationInPlaylist(
        stationID: String, playlistName: String, resultHandler : (Boolean) -> Unit){
        val check = repository.checkIfAlreadyInPlaylist(stationID, playlistName)

        resultHandler(check)

        if(!check){
            repository.insertStationPlaylistCrossRef(
                StationPlaylistCrossRef(
                    stationID, playlistName, System.currentTimeMillis()
                )
            )
            repository.incrementInPlaylistsCount(stationID)
        }
    }


    private fun addMediaItemOnDropToPlaylist(){
        radioServiceConnection.sendCommand(COMMAND_ON_DROP_STATION_IN_PLAYLIST, null)
    }



    val listOfAllPlaylists = repository.getAllPlaylists()

    private suspend fun getPlaylistOrder(playlistName: String) = repository.getPlaylistOrder(playlistName)

    @OptIn(ExperimentalCoroutinesApi::class)
    var stationsInPlaylistFlow = currentPlaylistName.asFlow().flatMapLatest { playlistName ->
        repository.getStationsInPlaylistFlow(playlistName).map {
            it?.let {
                val list = sortStationsInPlaylist(it.radioStations, playlistName)
                RadioSource.updateVisualPlaylist(list)
                list
            } ?: emptyList()
        }
    }

    private suspend fun sortStationsInPlaylist(stations : List<RadioStation>, playlistName: String)
    : List<RadioStation>
    {
        val result: MutableList<RadioStation> = mutableListOf()
        val stationIndexMap = stations.withIndex().associate { it.value.stationuuid to it.index }
        val order = getPlaylistOrder(playlistName)
        order.forEach { crossref ->
            val index = stationIndexMap[crossref.stationuuid]
            if (index != null) {
                result.add(stations[index])
            }
        }
        return result
    }


    val playlist : MutableLiveData<List<RadioStation>> = MutableLiveData()

    val favFragStationsSwitch = MutableLiveData(SEARCH_FROM_FAVOURITES)



    private var isToGenerateLazyList = true

    private val lazyListFlow = flow{
        if(isToGenerateLazyList){
            isToGenerateLazyList = false
            val list = repository.getStationsForLazyPlaylist()
            RadioSource.initiateLazyList(list)
            emit(list)
        } else {
            val list = RadioSource.lazyListStations.toList()
            emit(list)
        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val favFragStationsFlow = favFragStationsSwitch.asFlow().flatMapLatest {
        when (it) {
            SEARCH_FROM_FAVOURITES -> {
                getAllFavStationsFlow
            }
            SEARCH_FROM_PLAYLIST -> stationsInPlaylistFlow
            else -> lazyListFlow
        }
    }


    private var getAllFavStationsFlow = repository.getAllFavStationsFlow()


    fun subscribeToStationsInPlaylist(playlistName: String)
            = viewModelScope.launch {

            favFragStationsSwitch.value = SEARCH_FROM_PLAYLIST

            currentPlaylistName.value = playlistName
    }




    fun getAllFavouredStations() = viewModelScope.launch {

        favFragStationsSwitch.value = SEARCH_FROM_FAVOURITES

    }

    private var lazyListName = ""

    fun setLazyListName(name : String){
        lazyListName = name
    }


    fun getLazyPlaylist() {

        favFragStationsSwitch.value = SEARCH_FROM_LAZY_LIST

        currentPlaylistName.value = lazyListName

    }


    fun exportStationFromLazyList(playlistName : String) =
        viewModelScope.launch{

        for(i in RadioSource.lazyListStations.indices){
            handleCheckAndInsertStationInPlaylist(
                stationID = RadioSource.lazyListStations[i].stationuuid,
                playlistName = playlistName
            ) {}
        }

            if(RadioService.currentMediaItems == SEARCH_FROM_LAZY_LIST ||
                    RadioService.currentMediaItems == SEARCH_FROM_PLAYLIST &&
                    playlistName == RadioService.currentPlaylistName){
                radioServiceConnection.sendCommand(COMMAND_CLEAR_MEDIA_ITEMS, null)
            }

            RadioSource.clearLazyList()
            isToGenerateLazyList = true
            favFragStationsSwitch.value = SEARCH_FROM_LAZY_LIST

    }


    fun deletePlaylistAndContent(playlistName: String) =
        viewModelScope.launch {

            val stationsIds = repository.getStationsIdsFromPlaylist(playlistName)

            repository.deleteAllCrossRefOfPlaylist(playlistName)

           stationsIds.forEach {
               repository.decrementInPlaylistsCount(it)
           }

            repository.deletePlaylist(playlistName)

        }



    // For editing playlist

     fun editPlaylistCover(playlistName : String, newCover : String) = viewModelScope.launch {

         repository.editPlaylistCover(playlistName, newCover)
     }

     fun editPlaylistName(oldName : String, newName : String) = viewModelScope.launch {

         repository.editPlaylistName(oldName, newName)
     }

     fun editOldCrossRefWithPlaylist(oldName : String, newName : String) = viewModelScope.launch {

         repository.editOldCrossRefWithPlaylist(oldName, newName)
     }


    // COMMANDS

    fun updateFavPlaylist(){
        radioServiceConnection.sendCommand(COMMAND_UPDATE_FAV_PLAYLIST, null)
    }

    fun clearMediaItems(){
        radioServiceConnection.sendCommand(COMMAND_CLEAR_MEDIA_ITEMS, null)
    }


    private fun removeMediaItem(index : Int){
        radioServiceConnection.sendCommand(
            COMMAND_REMOVE_MEDIA_ITEM,
            bundleOf(Pair(Constants.ITEM_INDEX, index)))
    }

//    fun restoreMediaItem(index : Int){
//        radioServiceConnection.sendCommand(
//            COMMAND_RESTORE_MEDIA_ITEM,
//            bundleOf(Pair(Constants.ITEM_INDEX, index)))
//    }



    val onSwipeDeleteHandled = radioServiceConnection.onSwipeHandled.receiveAsFlow()


    fun onSwipeDeleteStation(index : Int, stationID: String){

        radioServiceConnection.sendCommand(
            COMMAND_ON_SWIPE_DELETE,
            bundleOf(
                Pair(Constants.ITEM_INDEX, index),
                Pair(ITEM_PLAYLIST, favFragStationsSwitch.value),
                Pair(ITEM_PLAYLIST_NAME, currentPlaylistName.value),
                Pair(ITEM_ID, stationID)
            )
        )
    }

    fun onRestoreStation(){
        radioServiceConnection.sendCommand(
            COMMAND_ON_SWIPE_RESTORE, null
        )

        if(favFragStationsSwitch.value == SEARCH_FROM_LAZY_LIST){
            getLazyPlaylist()
        }
    }


    fun onDragAndDropStation(
        index: Int,
        stationID: String,
        targetPlaylistName: String,
        onResult: (Boolean) -> Unit
    ){

        if(favFragStationsSwitch.value == SEARCH_FROM_PLAYLIST && targetPlaylistName == currentPlaylistName.value)
            return

        viewModelScope.launch {

            if(favFragStationsSwitch.value == RadioService.currentMediaItems){
                if(favFragStationsSwitch.value != SEARCH_FROM_PLAYLIST || currentPlaylistName.value == RadioService.currentPlaylistName){
                    removeMediaItem(index)
                }
            }

            when(favFragStationsSwitch.value){

                SEARCH_FROM_FAVOURITES -> {
                    repository.updateIsFavouredState(0, stationID)
                }

                SEARCH_FROM_PLAYLIST -> {
                    repository.decrementInPlaylistsCount(stationID)
                    repository.deleteStationPlaylistCrossRef(stationID, currentPlaylistName.value ?: "")
                }

                SEARCH_FROM_LAZY_LIST -> {
                    RadioSource.removeItemFromLazyList(index)
                    getLazyPlaylist()
                }
            }

            checkAndInsertStationPlaylistCrossRef(stationID, targetPlaylistName){ isSuccess ->

                if(isSuccess && RadioService.currentPlaylistName == currentPlaylistName.value &&
                    RadioService.currentMediaItems == SEARCH_FROM_PLAYLIST)
                    addMediaItemOnDropToPlaylist()

                onResult(isSuccess)

            }
        }
    }


}