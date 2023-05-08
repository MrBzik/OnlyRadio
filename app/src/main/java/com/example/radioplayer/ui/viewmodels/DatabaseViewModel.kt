package com.example.radioplayer.ui.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.example.radioplayer.adapters.datasources.*
import com.example.radioplayer.adapters.models.StationWithDateModel
import com.example.radioplayer.adapters.models.TitleWithDateModel
import com.example.radioplayer.data.local.entities.*
import com.example.radioplayer.data.local.relations.StationDateCrossRef
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.utils.Constants.HISTORY_3_DATES
import com.example.radioplayer.utils.Constants.HISTORY_OPTIONS
import com.example.radioplayer.utils.Constants.PAGE_SIZE
import com.example.radioplayer.utils.Utils.fromDateToString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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

        if(initialDate.isBlank()){

            val date = repository.getLastDate()
            date?.let {
                initialDate = it.date
                RadioService.currentDateLong = it.time
            }
        }

        if (update == initialDate) {/*DO NOTHING*/
            Log.d("CHECKTAGS", "update is $update")
        } else {
            initialDate = update

            RadioService.currentDateLong = newTime
            compareDatesWithPrefAndCLeanIfNeeded(HistoryDate(update, newTime))
        }

         repository.insertStationDateCrossRef(StationDateCrossRef(stationID, update))

    }


    // For RecyclerView

    val listOfDates = repository.getListOfDates

    var selectedDate = 0L

    private suspend fun getStationsInDate(limit: Int, offset: Int): List<StationWithDateModel> {

        Log.d("CHECKTAGS", "getting all stations")

        val response = radioSource.getStationsInAllDates(limit, offset, initialDate)

        val date = response.date.date

        val stationsWithDate: MutableList<StationWithDateModel> = mutableListOf()

        stationsWithDate.add(StationWithDateModel.DateSeparator(date))

        response.radioStations.reversed().forEach {
            stationsWithDate.add(StationWithDateModel.Station(it))
        }

        stationsWithDate.add(StationWithDateModel.DateSeparatorEnclosing(date))

        return stationsWithDate
    }

    private suspend fun getStationsInOneDate() : List<StationWithDateModel> {

        Log.d("CHECKTAGS", "getting stations in one date")

        val response = radioSource.getStationsInOneDate(selectedDate)

        val stationsWithDate: MutableList<StationWithDateModel> = mutableListOf()

        response.radioStations.reversed().forEach {
            stationsWithDate.add(StationWithDateModel.Station(it))
        }
        return stationsWithDate
    }



    private var lastTitleDate = 0L
    private var isTitleHeaderSet = false
    private var dateToString = ""

    private suspend fun getTitlesInAllDates(pageIndex : Int, pageSize : Int) : List<TitleWithDateModel>{

        Log.d("CHECKTAGS", "getting all titles")

        val calendar = Calendar.getInstance()
        val response = repository.getTitlesPage(pageIndex * PAGE_SIZE, pageSize)
        val titlesWithDates: MutableList<TitleWithDateModel> = mutableListOf()

        response.forEach { title ->

            if(title.date != lastTitleDate){

                if(isTitleHeaderSet) {
                    titlesWithDates.add(TitleWithDateModel.TitleDateSeparatorEnclosing(dateToString))
                }
                calendar.time = Date(title.date)
                dateToString = fromDateToString(calendar)
                titlesWithDates.add(TitleWithDateModel.TitleDateSeparator(dateToString))
                isTitleHeaderSet = true
                lastTitleDate = title.date

            }
            titlesWithDates.add(TitleWithDateModel.TitleItem(title))
        }

        if(response.size < PAGE_SIZE && isTitleHeaderSet){
            titlesWithDates.add(TitleWithDateModel.TitleDateSeparatorEnclosing(dateToString))
            isTitleHeaderSet = false
        }

        return titlesWithDates
    }


    private suspend fun getTitlesInOneDate(pageIndex : Int, pageSize : Int): List<TitleWithDateModel> {

        Log.d("CHECKTAGS", "getting titles in one date")

        val response = repository.getTitlesInOneDatePage(pageIndex * PAGE_SIZE, pageSize, selectedDate)
        val titlesWithDates: MutableList<TitleWithDateModel> = mutableListOf()

        response.forEach { title ->
            titlesWithDates.add(TitleWithDateModel.TitleItem(title))
        }

        return titlesWithDates
    }



//     val updateHistory : MutableLiveData<Boolean> = MutableLiveData()
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val historyFlow = updateHistory.asFlow()
//        .flatMapLatest {
//            if(it)
//            stationsHistoryFlow()
//            else stationsHistoryOneDateFlow()
//        }.cachedIn(viewModelScope)



    var isHistoryInStationsTab = true

    var isHistoryTitlesInBookmark = false


    private val allHistoryLoader : HistoryDateLoader = { dateIndex ->
        getStationsInDate(1, dateIndex)
    }

    private val oneDateHistoryLoader : HistoryOneDateLoader = {
        getStationsInOneDate()
    }


    private val allTitlesLoader : TitlesPageLoader = { pageIndex, pageSize ->
        getTitlesInAllDates(pageIndex, pageSize)
    }

    private val oneDateTitleLoader : TitlesPageLoader = { pageIndex, pageSize ->
        getTitlesInOneDate(pageIndex, pageSize)
    }




    var historyFlow : LiveData<PagingData<StationWithDateModel>>?  = null
    private var oneDateHistoryFlow : LiveData<PagingData<StationWithDateModel>>?  = null

    var titlesFlow : LiveData<PagingData<TitleWithDateModel>>? = null
    private var oneDateTitlesFlow : LiveData<PagingData<TitleWithDateModel>>? = null


    val observableHistory = MediatorLiveData<PagingData<StationWithDateModel>>()

    val observableTitles = MediatorLiveData<PagingData<TitleWithDateModel>>()

    val oneHistoryDateCaller : MutableLiveData<Boolean> = MutableLiveData(false)

    val oneTitleDateCaller : MutableLiveData<Boolean> = MutableLiveData(false)

    fun setTitlesLiveData(lifecycle: CoroutineScope){

        titlesFlow = Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                enablePlaceholders = false
            ), pagingSourceFactory = {
                TitlesDataSource(allTitlesLoader, PAGE_SIZE)
            }
        ).liveData.cachedIn(lifecycle)

        oneDateTitlesFlow = Transformations.switchMap(oneTitleDateCaller){
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    initialLoadSize = PAGE_SIZE,
                    enablePlaceholders = false
                ), pagingSourceFactory = {
                    TitlesDataSource(oneDateTitleLoader, PAGE_SIZE)
                }
            ).liveData.cachedIn(lifecycle)
        }


        observableTitles.addSource(titlesFlow!!) { titles ->
            if(selectedDate == 0L){
                observableTitles.value = titles
            }
        }

        observableTitles.addSource(oneDateTitlesFlow!!) { titles ->
            if(selectedDate > 0L){
                observableTitles.value = titles
            }
        }

    }


    fun setHistoryLiveData(lifecycle: CoroutineScope){

        historyFlow = Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                HistoryDataSource(allHistoryLoader)
            }
        ).liveData.cachedIn(lifecycle)

        oneDateHistoryFlow = Transformations.switchMap(oneHistoryDateCaller){

               Pager(
                    config = PagingConfig(
                        pageSize = 10,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = {
                        HistoryOneDateSource(oneDateHistoryLoader)
                    }
                ).liveData.cachedIn(lifecycle)

        }

        observableHistory.addSource(historyFlow!!) { history ->
            if(selectedDate == 0L){
                observableHistory.value = history
            }
        }

        observableHistory.addSource(oneDateHistoryFlow!!) { history ->
            if(selectedDate > 0L){
                observableHistory.value = history
            }
        }

    }

    fun getAllHistory(){

        historyFlow?.value?.let {
            observableHistory.value = it
        }

    }


    fun getAllTitles(){

        titlesFlow?.value?.let {
            observableTitles.value = it
        }

    }

//    fun updateBookmarkedState(isBookmarked : Boolean, title : String) = viewModelScope.launch{
//        repository.updateBookmarkedState(isBookmarked, title)
//    }




    fun cleanHistory(){

        historyFlow?.let{
            observableHistory.removeSource(it)
        }
        oneDateHistoryFlow?.let {
            observableHistory.removeSource(it)
        }

       titlesFlow?.let {
           observableTitles.removeSource(it)
       }

        oneDateTitlesFlow?.let {
            observableTitles.removeSource(it)
        }

        historyFlow = null
        oneDateHistoryFlow = null
        titlesFlow = null
        oneDateTitlesFlow = null

        lastTitleDate = 0L
        isTitleHeaderSet = false

    }


    // Bookmarked title

    val bookmarkedTitlesLivedata = repository.bookmarkedTitlesLiveData()

    fun upsertBookmarkedTitle(title : Title) = viewModelScope.launch {

        repository.deleteBookmarkedTitle(title.title)

        repository.insertNewBookmarkedTitle(
            BookmarkedTitle(
                timeStamp = title.timeStamp,
                date = title.date,
                title = title.title,
                stationName = title.stationName,
                stationIconUri = title.stationIconUri
            )
        )
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
                repository.deleteTitlesWithDate(it.time)
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