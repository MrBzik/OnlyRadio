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
import com.onlyradio.radioplayer.repositories.DatabaseRepository
import com.onlyradio.radioplayer.utils.Commands.COMMAND_ADD_MEDIA_ITEM
import com.onlyradio.radioplayer.utils.Commands.COMMAND_CLEAR_MEDIA_ITEMS
import com.onlyradio.radioplayer.utils.Commands.COMMAND_ON_DROP_STATION_IN_PLAYLIST
import com.onlyradio.radioplayer.utils.Commands.COMMAND_REMOVE_MEDIA_ITEM
import com.onlyradio.radioplayer.utils.Commands.COMMAND_UPDATE_FAV_PLAYLIST
import com.onlyradio.radioplayer.utils.Constants
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_LAZY_LIST
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DatabaseViewModel @Inject constructor(
        private val repository: DatabaseRepository,
        private val radioSource: RadioSource,
        private val radioServiceConnection: RadioServiceConnection
) : ViewModel() {



//    val navBarHeight : Int by lazy {
//        Utils.getNavigationBarHeight(getApplication())
//    }
//
//    val statusBarHeight : Int by lazy {
//        Utils.getStatusBarHeight(getApplication())
//    }


//    fun getRadioStationPlayDuration(stationID: String, handleResult: (Long) -> Unit) =
//        viewModelScope.launch {
//            repository.getRadioStationPlayDuration(stationID)?.also {result ->
//                handleResult(result)
//            }
//        }




    val isStationFavoured: MutableLiveData<Boolean> = MutableLiveData()

    var currentPlaylistName: MutableLiveData<String> = MutableLiveData("")


    fun checkIfStationIsFavoured(stationID: String) = viewModelScope.launch {
        val check = repository.checkIfStationIsFavoured(stationID)
        isStationFavoured.postValue(check)
    }

    fun updateIsFavouredState(value: Long, stationID: String) = viewModelScope.launch {
        repository.updateIsFavouredState(value, stationID)
    }



//    fun insertRadioStation(station: RadioStation) = viewModelScope.launch {
//        repository.insertRadioStation(station)
//    }

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


    fun addMediaItemOnDropToPlaylist(){
        radioServiceConnection.sendCommand(COMMAND_ON_DROP_STATION_IN_PLAYLIST, null)
    }


    fun insertStationPlaylistCrossRef(
                    crossRef: StationPlaylistCrossRef
                    ) = viewModelScope.launch {
        repository.incrementInPlaylistsCount(crossRef.stationuuid)
        repository.insertStationPlaylistCrossRef(crossRef)
    }

    suspend fun getTimeOfStationPlaylistInsertion(stationID : String, playlistName : String)
            = repository.getTimeOfStationPlaylistInsertion(stationID, playlistName)

    fun deleteStationPlaylistCrossRef(stationID: String, playlistName: String) = viewModelScope.launch {
        repository.decrementInPlaylistsCount(stationID)
        repository.deleteStationPlaylistCrossRef(stationID, playlistName)
    }


    val listOfAllPlaylists = repository.getAllPlaylists()

//    private val stationInFavoured = repository.getAllFavouredStations()

//    private val stationsInLazyPlaylist = repository.getStationsForLazyPlaylist()


//     var stationsPlaylistOrder = Transformations.switchMap(
//        currentPlaylistName) { playlistName ->
//
//        repository.subscribeToPlaylistOrder(playlistName)
//
//
//    }


    suspend fun getPlaylistOrder(playlistName: String) = repository.getPlaylistOrder(playlistName)

//     var stationsInPlaylist = Transformations.switchMap(
//        currentPlaylistName) { playlistName ->
//                repository.subscribeToStationsInPlaylist(playlistName)
//        }


    @OptIn(ExperimentalCoroutinesApi::class)
    var stationsInPlaylistFlow = currentPlaylistName.asFlow().flatMapLatest { playlistName ->
        repository.getStationsInPlaylistFlow(playlistName).map {
            it?.let {
                sortStationsInPlaylist(it.radioStations, playlistName)
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


//    var isInFavouriteTab: MutableLiveData<Boolean> = MutableLiveData(true)

    var isInLazyPlaylist = false

//    val observableListOfStations = MediatorLiveData<List<RadioStation>>()

    val playlist : MutableLiveData<List<RadioStation>> = MutableLiveData()

//    var isLazyPlaylistSourceSet = false

    val favFragStationsSwitch = MutableLiveData(SEARCH_FROM_FAVOURITES)



    var isToGenerateLazyList = true

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

    var getAllFavStationsFlow = repository.getAllFavStationsFlow()

//    init {
//
//        observableListOfStations.addSource(stationInFavoured) { favStations ->
//            if (isInFavouriteTab.value == true) {
//                Log.d("CHECKTAGS", "on change of fav stations")
//                observableListOfStations.value = favStations
//
//            }
//        }
//        observableListOfStations.addSource(playlist) { playlistStations ->
//            if (isInFavouriteTab.value == false && !isInLazyPlaylist) {
//
//                observableListOfStations.value = playlistStations
//            }
//        }
//
//    }





    fun subscribeToStationsInPlaylist(playlistName: String)
            = viewModelScope.launch {

            favFragStationsSwitch.postValue(SEARCH_FROM_PLAYLIST)

            currentPlaylistName.postValue(playlistName)
//            isInLazyPlaylist = false
//            isInFavouriteTab.postValue(false)

            // to update service
            radioSource.getStationsInPlaylist(playlistName)
    }




    fun getAllFavouredStations() = viewModelScope.launch {

        favFragStationsSwitch.postValue(SEARCH_FROM_FAVOURITES)

//        isInLazyPlaylist = false
//        isInFavouriteTab.postValue(true)

//        observableListOfStations.value = stationInFavoured.value
    }


    fun getLazyPlaylist(name : String) {

        favFragStationsSwitch.postValue(SEARCH_FROM_LAZY_LIST)

//        isInLazyPlaylist = true
//        isInFavouriteTab.postValue(false)
        currentPlaylistName.postValue(
            name
        )

//        if(isLazyPlaylistSourceSet){
//            observableListOfStations.value = stationsInLazyPlaylist.value
//        } else {
//            observableListOfStations.addSource(stationsInLazyPlaylist){ lazyPlaylist ->
//                Log.d("CHECKTAGS", "on change of lazy playlist")
//                observableListOfStations.value = lazyPlaylist
//            }
//            isLazyPlaylistSourceSet = true
//        }
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
            favFragStationsSwitch.postValue(SEARCH_FROM_LAZY_LIST)

    }


    fun clearRadioStationPlayedDuration(stationID: String) = viewModelScope.launch{
        repository.clearRadioStationPlayedDuration(
            stationID
        )
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


    fun removeMediaItem(index : Int){
        radioServiceConnection.sendCommand(
            COMMAND_REMOVE_MEDIA_ITEM,
            bundleOf(Pair(Constants.ITEM_INDEX, index)))
    }

    fun restoreMediaItem(index : Int){
        radioServiceConnection.sendCommand(
            COMMAND_ADD_MEDIA_ITEM,
            bundleOf(Pair(Constants.ITEM_INDEX, index)))
    }


}