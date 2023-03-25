package com.example.radioplayer.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.radioplayer.adapters.datasources.HistoryDataSource
import com.example.radioplayer.adapters.datasources.HistoryDateLoader
import com.example.radioplayer.adapters.datasources.HistoryOneDateLoader
import com.example.radioplayer.adapters.datasources.HistoryOneDateSource
import com.example.radioplayer.adapters.models.StationWithDateModel
import com.example.radioplayer.data.local.entities.HistoryDate
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.data.local.relations.StationDateCrossRef
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.utils.Constants.HISTORY_3_DATES
import com.example.radioplayer.utils.Constants.HISTORY_OPTIONS
import com.example.radioplayer.utils.Utils
import com.example.radioplayer.utils.Utils.fromDateToString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.sql.Date
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DatabaseViewModel @Inject constructor(
        private val app : Application,
        private val repository: DatabaseRepository,
        private val radioSource: RadioSource
) : AndroidViewModel(app) {



//    val navBarHeight : Int by lazy {
//        Utils.getNavigationBarHeight(getApplication())
//    }
//
//    val statusBarHeight : Int by lazy {
//        Utils.getStatusBarHeight(getApplication())
//    }


    val isStationFavoured: MutableLiveData<Boolean> = MutableLiveData()

    var currentPlaylistName: MutableLiveData<String> = MutableLiveData("")

    var isCleanUpNeeded = false



    fun checkIfStationIsFavoured(stationID: String) = viewModelScope.launch {
        val check = repository.checkIfStationIsFavoured(stationID)
        isStationFavoured.postValue(check)
    }

    fun updateIsFavouredState(value: Long, stationID: String) = viewModelScope.launch {
        repository.updateIsFavouredState(value, stationID)
    }



    fun insertRadioStation(station: RadioStation) = viewModelScope.launch {
        repository.insertRadioStation(station)
    }

    fun insertNewPlayList(playlist: Playlist) = viewModelScope.launch {
        repository.insertNewPlaylist(playlist)
    }


    fun insertStationPlaylistCrossRef(
                    crossRef: StationPlaylistCrossRef
                    ) = viewModelScope.launch {
        repository.insertStationPlaylistCrossRef(crossRef)

    }

    suspend fun getTimeOfStationPlaylistInsertion(stationID : String, playlistName : String)
            = repository.getTimeOfStationPlaylistInsertion(stationID, playlistName)

    fun deleteStationPlaylistCrossRef(stationID: String, playlistName: String) = viewModelScope.launch {
        repository.deleteStationPlaylistCrossRef(stationID, playlistName)
    }


    val listOfAllPlaylists = repository.getAllPlaylists()



    private val stationInFavoured = repository.getAllFavouredStations()


//     var stationsPlaylistOrder = Transformations.switchMap(
//        currentPlaylistName) { playlistName ->
//
//        repository.subscribeToPlaylistOrder(playlistName)
//
//
//    }


    suspend fun getPlaylistOrder(playlistName: String) = repository.getPlaylistOrder(playlistName)


     var stationsInPlaylist = Transformations.switchMap(
        currentPlaylistName) { playlistName ->
                repository.subscribeToStationsInPlaylist(playlistName)
        }


    var isInFavouriteTab: MutableLiveData<Boolean> = MutableLiveData(true)

    val observableListOfStations = MediatorLiveData<List<RadioStation>>()


    val playlist : MutableLiveData<List<RadioStation>> = MutableLiveData()


    init {

        observableListOfStations.addSource(stationInFavoured) { favStations ->
            if (isInFavouriteTab.value == true) {
                observableListOfStations.value = favStations

            }
        }
        observableListOfStations.addSource(playlist) { playlistStations ->
            if (isInFavouriteTab.value == false) {
                observableListOfStations.value = playlistStations

            }
        }
    }


    fun subscribeToStationsInPlaylist(playlistName: String)
            = viewModelScope.launch {

            currentPlaylistName.postValue(playlistName)
            isInFavouriteTab.postValue(false)

            // to update service
            radioSource.getStationsInPlaylist(playlistName)
    }




    fun getAllFavouredStations() = viewModelScope.launch {

        isInFavouriteTab.postValue(true)

        observableListOfStations.value = stationInFavoured.value

    }


    fun deletePlaylistAndContent(playlistName: String) =
        viewModelScope.launch {

            repository.deleteAllCrossRefOfPlaylist(playlistName)

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



    // date



     var initialDate: String = ""
    private val calendar = Calendar.getInstance()


    fun checkDateAndUpdateHistory(stationID: String) = viewModelScope.launch {

        val newTime = System.currentTimeMillis()
        calendar.time = Date(newTime)
        val update = fromDateToString(calendar)

        if (update == initialDate) {/*DO NOTHING*/
        } else {
            val check = repository.checkLastDateRecordInDB(update)
            if (!check) {
                initialDate = update
                compareDatesWithPrefAndCLeanIfNeeded(HistoryDate(update, newTime))
            }
        }

        repository.insertStationDateCrossRef(StationDateCrossRef(stationID, update))
//        updateHistory.postValue(true)
    }


    // For RecyclerView

    val listOfDates = repository.getListOfDates

    var selectedDate = -1L

    private suspend fun getStationsInDate(limit: Int, offset: Int): List<StationWithDateModel> {

        val response = radioSource.getStationsInDate(limit, offset, initialDate)

        val date = response.date.date

        val stationsWithDate: MutableList<StationWithDateModel> = mutableListOf()

        stationsWithDate.add(StationWithDateModel.DateSeparator(date))

        response.radioStations.reversed().forEach {
            stationsWithDate.add(StationWithDateModel.Station(it))
        }

        stationsWithDate.add(StationWithDateModel.DateSeparatorEnclosing(date))

        return stationsWithDate
    }

    private suspend fun getStationsInOneDate(time : Long) : List<StationWithDateModel> {

        val response = radioSource.getStationsInOneDate(time)

        val stationsWithDate: MutableList<StationWithDateModel> = mutableListOf()

        response.radioStations.reversed().forEach {
            stationsWithDate.add(StationWithDateModel.Station(it))
        }
        return stationsWithDate
    }




     val updateHistory : MutableLiveData<Boolean> = MutableLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyFlow = updateHistory.asFlow()
        .flatMapLatest { if(it)
            stationsHistoryFlow()
            else stationsHistoryOneDateFlow()
        }.cachedIn(viewModelScope)


    private fun stationsHistoryOneDateFlow() : Flow<PagingData<StationWithDateModel>> {
        val loader : HistoryOneDateLoader = {
            getStationsInOneDate(selectedDate)
        }

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                HistoryOneDateSource(loader)
            }
        ).flow
    }


    private fun stationsHistoryFlow() : Flow<PagingData<StationWithDateModel>> {
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
        ).flow
    }





    // Handle history options and cleaning history

    private val historyOptionsPref = app.getSharedPreferences(HISTORY_OPTIONS, Context.MODE_PRIVATE)

    private val editor = historyOptionsPref.edit()

    fun getHistoryOptionsPref() : Int {

        return historyOptionsPref.getInt(HISTORY_OPTIONS, HISTORY_3_DATES)
    }

    fun setHistoryOptionsPref(newOption : Int) {

        editor.putInt(HISTORY_OPTIONS, newOption)
        editor.commit()
    }


    fun compareDatesWithPrefAndCLeanIfNeeded(newDate: HistoryDate?)
            = viewModelScope.launch {

        newDate?.let {
            repository.insertNewDate(newDate)
        }

        val pref = getHistoryOptionsPref()

//        if(pref == HISTORY_NEVER_CLEAN) return@launch


        val numberOfDatesInDB =  repository.getNumberOfDates()

        if(pref >= numberOfDatesInDB) return@launch
        else {
            isCleanUpNeeded = true
            val numberOfDatesToDelete = numberOfDatesInDB - pref
            val deleteList = repository.getDatesToDelete(numberOfDatesToDelete)

            deleteList.forEach {
                repository.deleteAllCrossRefWithDate(it.date)
                repository.deleteDate(it)
            }
        }
//            updateHistory.postValue(true)

    }




    // Cleaning up database

    fun removeUnusedStations() = viewModelScope.launch {

        if(isCleanUpNeeded){

            val stations = repository.gatherStationsForCleaning()

            stations.forEach {

                    val checkIfInPlaylists = repository.checkIfInPlaylists(it.stationuuid)

                    if(!checkIfInPlaylists) {

                        val checkIfInHistory =  repository.checkIfRadioStationInHistory(it.stationuuid)

                        if(!checkIfInHistory){

                            repository.deleteRadioStation(it)
                        }
                    }
                }

            isCleanUpNeeded = false
        }
    }


    // Recordings

//    fun insertNewRecording (
//        recordID : String,
//        iconURI : String,
//        name : String,
//        duration : String
//        ) = viewModelScope.launch {
//            repository.insertRecording(
//                Recording(
//                    id = recordID,
//                    iconUri = iconURI,
//                    name = name,
//                    timeStamp = System.currentTimeMillis(),
//                    duration = duration)
//            )
//    }

    fun insertNewRecording(rec : Recording) =
        viewModelScope.launch {
            repository.insertRecording(rec)
        }

    fun deleteRecording(recId : String) = viewModelScope.launch {
        repository.deleteRecording(recId)
    }


     val allRecordingsLiveData = radioSource.allRecordingsLiveData







    fun removeRecordingFile(recordingID : String){
        app.deleteFile(recordingID)
    }

    fun renameRecording(id : String, newName: String) = viewModelScope.launch {
        repository.renameRecording(id, newName)
    }

}