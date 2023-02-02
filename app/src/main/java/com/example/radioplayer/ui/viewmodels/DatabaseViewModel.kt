package com.example.radioplayer.ui.viewmodels

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.radioplayer.adapters.datasources.HistoryDataSource
import com.example.radioplayer.adapters.datasources.HistoryDateLoader
import com.example.radioplayer.adapters.models.StationWithDateModel
import com.example.radioplayer.data.local.entities.Date
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.StationDateCrossRef
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.exoPlayer.RadioServiceConnection
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.utils.Constants.COMMAND_LOAD_FROM_PLAYLIST
import com.example.radioplayer.utils.Constants.DATE_FORMAT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

@HiltViewModel
class DatabaseViewModel @Inject constructor(
        private val repository: DatabaseRepository,
        private val radioSource: RadioSource,
        private val radioServiceConnection: RadioServiceConnection
) : ViewModel() {


    val isStationInDB: MutableLiveData<Boolean> = MutableLiveData()

    val isStationFavoured: MutableLiveData<Boolean> = MutableLiveData()

    var currentPlaylistName: MutableLiveData<String> = MutableLiveData()


    fun ifStationAlreadyInDatabase(stationID: String) = viewModelScope.launch {
        val check = repository.checkIfRadioStationInDB(stationID)
        isStationInDB.postValue(check)
    }

    fun checkIfStationIsFavoured(stationID: String) = viewModelScope.launch {
        val check = repository.checkIfStationIsFavoured(stationID)
        isStationFavoured.postValue(check)
    }

    fun updateIsFavouredState(value: Int, stationID: String) = viewModelScope.launch {
        repository.updateIsFavouredState(value, stationID)
    }


    fun deleteStation(station: RadioStation) = viewModelScope.launch {
        repository.deleteRadioStation(station)
    }

    fun insertRadioStation(station: RadioStation) = viewModelScope.launch {
        repository.insertRadioStation(station)
    }

    fun insertNewPlayList(playlist: Playlist) = viewModelScope.launch {
        repository.insertNewPlaylist(playlist)
    }


    fun checkIfInPlaylistOrIncrement(playlistName: String, stationID: String) =
        viewModelScope.launch {
            val check = repository.checkIfInPlaylist(playlistName, stationID)
            if (!check) {
                incrementRadioStationPlaylist(stationID)
            }
        }

    fun insertStationPlaylistCrossRef(crossRef: StationPlaylistCrossRef) = viewModelScope.launch {
        repository.insertStationPlaylistCrossRef(crossRef)

    }

    fun deleteStationPlaylistCrossRef(crossRef: StationPlaylistCrossRef) = viewModelScope.launch {
        repository.deleteStationPlaylistCrossRef(crossRef)
    }

    fun incrementRadioStationPlaylist(stationID: String) = viewModelScope.launch {
        repository.incrementRadioStationPlaylist(stationID)
    }

    fun decrementRadioStationPlaylist(stationID: String) = viewModelScope.launch {
        repository.decrementRadioStationPlaylist(stationID)
    }

    suspend fun checkIfPlaylistExists(playlistName: String) =
        repository.checkIfPlaylistExists(playlistName)

    val listOfAllPlaylists = repository.getAllPlaylists()


    private val stationsInPlaylist: MutableLiveData<List<RadioStation>> = MutableLiveData()

    private val stationInFavoured = repository.getAllFavouredStations()

    var isInFavouriteTab: MutableLiveData<Boolean> = MutableLiveData(true)


    val observableListOfStations = MediatorLiveData<List<RadioStation>>()


    init {

        observableListOfStations.addSource(stationInFavoured) { favStations ->
            if (isInFavouriteTab.value!!) {
                observableListOfStations.value = favStations

            }
        }
        observableListOfStations.addSource(stationsInPlaylist) { playlistStations ->
            if (!isInFavouriteTab.value!!) {
                observableListOfStations.value = playlistStations

            }
        }

    }


    fun getStationsInPlaylist(playlistName: String, isForUpdate: Boolean = false) =
        viewModelScope.launch {

            if (!isForUpdate) {

                currentPlaylistName.postValue(playlistName)
                isInFavouriteTab.postValue(false)
            }

            val playlist = radioSource.getStationsInPlaylist(playlistName)

            stationsInPlaylist.postValue(playlist)

            sendServiceCommandToUpdatePlaylist()

        }


    fun getAllFavouredStations() = viewModelScope.launch {

        isInFavouriteTab.postValue(true)

        observableListOfStations.value = stationInFavoured.value

    }


    fun deletePlaylistAndContent(playlistName: String, stations: List<RadioStation>) =
        viewModelScope.launch {

            stations.forEach {
                repository.decrementRadioStationPlaylist(it.stationuuid)
            }
            repository.deleteAllCrossRefOfPlaylist(playlistName)

            repository.deletePlaylist(playlistName)

        }


    private fun sendServiceCommandToUpdatePlaylist() {

        radioServiceConnection.sendCommand(COMMAND_LOAD_FROM_PLAYLIST, Bundle())

    }

    // date



    private val time = System.currentTimeMillis()
    private val format = SimpleDateFormat(DATE_FORMAT)
    private var initialDate: String = ""

    init {

        initialDate = format.format(time)

    }

    private var isDateInDBAlreadyInserted = false


    fun checkDateAndUpdateHistory(stationID: String) = viewModelScope.launch {

        val newTime = System.currentTimeMillis()
        val update = format.format(newTime)

        if (isDateInDBAlreadyInserted && update == initialDate) {/*DO NOTHING*/
        } else {
            val check = repository.checkLastDateRecordInDB(update)
            if (!check) {
                repository.insertNewDate(Date(update, newTime))
                initialDate = update
            }
            isDateInDBAlreadyInserted = true
        }

        repository.insertStationDateCrossRef(StationDateCrossRef(stationID, update))

    }


    // For RecyclerView

    private suspend fun getStationsInDate(limit: Int, offset: Int): List<StationWithDateModel> {

        val response = repository.getStationsInDate(limit, offset)

        val date = response.date.date

        val stationsWithDate: MutableList<StationWithDateModel> = mutableListOf()

        stationsWithDate.add(StationWithDateModel.DateSeparator(date))

        response.radioStations.reversed().forEach {
            stationsWithDate.add(StationWithDateModel.Station(it))
        }

        stationsWithDate.add(StationWithDateModel.DateSeparatorEnclosing(date))

        return stationsWithDate
    }



     fun getStationsHistory(): Flow<PagingData<StationWithDateModel>> {
        val loader : HistoryDateLoader = { dateIndex ->
            getStationsInDate(1, dateIndex)
        }

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                HistoryDataSource(loader)
            }
        ).flow.cachedIn(viewModelScope)
    }



}