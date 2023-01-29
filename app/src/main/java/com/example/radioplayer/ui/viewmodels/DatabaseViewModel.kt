package com.example.radioplayer.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.repositories.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatabaseViewModel @Inject constructor(
        private val repository: DatabaseRepository,
        private val radioSource: RadioSource,
) : ViewModel() {

    val getAllStationsTEST = radioSource.getAllItemsTEST()

    val isStationInDB : MutableLiveData<Boolean> = MutableLiveData()

    val isStationFavoured : MutableLiveData<Boolean> = MutableLiveData()

    var currentPlaylistName : MutableLiveData<String> = MutableLiveData()


    fun ifStationAlreadyInDatabase(stationID : String) = viewModelScope.launch {
       val check = repository.checkIfRadioStationInDB(stationID)
        isStationInDB.postValue(check)
    }

    fun checkIfStationIsFavoured (stationID: String) = viewModelScope.launch {
        val check = repository.checkIfStationIsFavoured(stationID)
        isStationFavoured.postValue(check)
    }

    fun updateIsFavouredState (value : Int, stationID: String) = viewModelScope.launch {
        repository.updateIsFavouredState(value, stationID)
    }




    fun deleteStation (station: RadioStation) = viewModelScope.launch {
        repository.deleteRadioStation(station)
    }

    fun insertRadioStation (station: RadioStation) = viewModelScope.launch {
        repository.insertRadioStation(station)
    }

    fun insertNewPlayList (playlist: Playlist) = viewModelScope.launch {
        repository.insertNewPlaylist(playlist)
    }

    fun deletePlaylist (playlist: Playlist) = viewModelScope.launch {
        repository.deletePlaylist(playlist)
    }

    fun checkIfInPlaylistOrIncrement (playlistName : String, stationID : String)
            = viewModelScope.launch {
              val check = repository.checkIfInPlaylist(playlistName, stationID)
            if(!check){
                incrementRadioStationPlaylist(stationID)
            }
    }

    fun insertStationPlaylistCrossRef (crossRef: StationPlaylistCrossRef)
            = viewModelScope.launch {
                repository.insertStationPlaylistCrossRef(crossRef)

    }

    fun deleteStationPlaylistCrossRef (crossRef: StationPlaylistCrossRef)
            = viewModelScope.launch {
                repository.deleteStationPlaylistCrossRef(crossRef)
    }

    fun incrementRadioStationPlaylist (stationID: String)
            = viewModelScope.launch {
                repository.incrementRadioStationPlaylist(stationID)
    }

    fun decrementRadioStationPlaylist (stationID: String)
            = viewModelScope.launch {
        repository.decrementRadioStationPlaylist(stationID)
    }

    suspend fun checkIfPlaylistExists (playlistName : String)
            = repository.checkIfPlaylistExists(playlistName)

    val listOfAllPlaylists = repository.getAllPlaylists()




    private val stationsInPlaylist : MutableLiveData<List<RadioStation>> = MutableLiveData()

    private val stationInFavoured = repository.getAllFavouredStations()

    var isInFavouriteTab : MutableLiveData<Boolean> = MutableLiveData(true)


    val observableListOfStations = MediatorLiveData<List<RadioStation>>()


    init {

        observableListOfStations.addSource(stationInFavoured){ favStations ->
            if(isInFavouriteTab.value!!){
                observableListOfStations.value = favStations

            }
        }
        observableListOfStations.addSource(stationsInPlaylist){ playlistStations ->
            if(!isInFavouriteTab.value!!){
                observableListOfStations.value = playlistStations

            }
        }

    }


    fun getStationsInPlaylist (playlistName: String, isSwipeUpdate : Boolean = false)
            = viewModelScope.launch {

        if(!isSwipeUpdate){

            currentPlaylistName.postValue(playlistName)
            isInFavouriteTab.postValue(false)
        }


        val response =  repository.getStationsInPlaylist(playlistName)
        val playlist = response.first().radioStations

        stationsInPlaylist.postValue(playlist)

    }



    fun getAllFavouredStations () = viewModelScope.launch {

        isInFavouriteTab.postValue(true)

        observableListOfStations.value = stationInFavoured.value

    }





}