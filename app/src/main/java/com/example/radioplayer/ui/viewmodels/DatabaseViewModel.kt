package com.example.radioplayer.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import androidx.paging.*
import com.example.radioplayer.adapters.datasources.*
import com.example.radioplayer.adapters.models.StationWithDateModel
import com.example.radioplayer.adapters.models.TitleWithDateModel

import com.example.radioplayer.data.local.entities.*
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.RadioServiceConnection
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.COMMAND_CLEAR_MEDIA_ITEMS
import com.example.radioplayer.utils.Constants.COMMAND_ON_DROP_STATION_IN_PLAYLIST
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_HISTORY_MEDIA_ITEMS
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_HISTORY_ONE_DATE_MEDIA_ITEMS
import com.example.radioplayer.utils.Constants.IS_TO_CLEAR_HISTORY_ITEMS
import com.example.radioplayer.utils.Constants.LAZY_LIST_NAME
import com.example.radioplayer.utils.Constants.PAGE_SIZE
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_LAZY_LIST
import com.example.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.example.radioplayer.utils.Utils.fromDateToString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.sql.Date
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DatabaseViewModel @Inject constructor(
        private val app : Application,
        private val repository: DatabaseRepository,
        private val radioSource: RadioSource,
        private val radioServiceConnection: RadioServiceConnection
) : AndroidViewModel(app) {



//    val navBarHeight : Int by lazy {
//        Utils.getNavigationBarHeight(getApplication())
//    }
//
//    val statusBarHeight : Int by lazy {
//        Utils.getStatusBarHeight(getApplication())
//    }


    fun getRadioStationPlayDuration(stationID: String, handleResult: (Long) -> Unit) =
        viewModelScope.launch {
        val result = repository.getRadioStationPlayDuration(stationID)
        handleResult(result)
    }




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
        Log.d("CHECKTAGS", "switch is activated?")

        when (it) {
            SEARCH_FROM_FAVOURITES -> {
                Log.d("CHECKTAGS", "getting all fav stations flow already?")
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


    fun getLazyPlaylist() {

        favFragStationsSwitch.postValue(SEARCH_FROM_LAZY_LIST)

//        isInLazyPlaylist = true
//        isInFavouriteTab.postValue(false)
        currentPlaylistName.postValue(LAZY_LIST_NAME)

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




    // Dates for RecyclerView

    val listOfDates = repository.getListOfDates

    var selectedDate = 0L

    private suspend fun getStationsInAllDates(limit: Int, offset: Int): List<StationWithDateModel> {

        val response = radioSource.getStationsInAllDates(limit, offset)

        if(RadioService.currentMediaItems == SEARCH_FROM_HISTORY){
            radioServiceConnection.sendCommand(COMMAND_UPDATE_HISTORY_MEDIA_ITEMS,
            bundleOf(Pair(IS_TO_CLEAR_HISTORY_ITEMS, offset == 0))
            )
        }


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

        val response = radioSource.getStationsInOneDate(selectedDate)

        val stations = response.radioStations.reversed()
        val date = response.date.date

        radioSource.updateStationsInOneDate(stations)

        val stationsWithDate: MutableList<StationWithDateModel> = mutableListOf()

        stationsWithDate.add(StationWithDateModel.DateSeparator(date))

        stations.forEach {
            stationsWithDate.add(StationWithDateModel.Station(it))
        }

        stationsWithDate.add(StationWithDateModel.DateSeparatorEnclosing(date))

        if(selectedDate == RadioService.currentDateLong &&
            RadioService.currentMediaItems == Constants.SEARCH_FROM_HISTORY_ONE_DATE
        ){
            RadioSource.updateHistoryOneDateStations()
            radioServiceConnection.sendCommand(COMMAND_UPDATE_HISTORY_ONE_DATE_MEDIA_ITEMS, null)
        }


        return stationsWithDate
    }



    private var lastTitleDate = 0L
    private var isTitleHeaderSet = false
    private var dateToString = ""

    private val calendar = Calendar.getInstance()

    private suspend fun getTitlesInAllDates(pageIndex : Int, pageSize : Int) : List<TitleWithDateModel>{

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


     var isTitleOneDateHeaderSet = false

    private suspend fun getTitlesInOneDate(pageIndex : Int, pageSize : Int): List<TitleWithDateModel> {

        val response = repository.getTitlesInOneDatePage(pageIndex * PAGE_SIZE, pageSize, selectedDate)
        val titlesWithDates: MutableList<TitleWithDateModel> = mutableListOf()

        if(!isTitleOneDateHeaderSet){
            isTitleOneDateHeaderSet = true
            calendar.time = Date(selectedDate)
            titlesWithDates.add(TitleWithDateModel.TitleDateSeparator(fromDateToString(calendar)))
        }

        response.forEach { title ->
            titlesWithDates.add(TitleWithDateModel.TitleItem(title))
        }

        if(response.size < PAGE_SIZE){
            titlesWithDates.add(TitleWithDateModel.TitleDateSeparatorEnclosing(fromDateToString(calendar)))
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



//    var isHistoryInStationsTab = true

//    var isHistoryTitlesInBookmark = false

    var isInBookmarksLiveData : MutableLiveData<Boolean> = MutableLiveData(false)
    var isHistoryInStationsTabLiveData : MutableLiveData<Boolean> = MutableLiveData(true)

    var isInBookmarks = false
    var isInStationsTab = true


    private val allHistoryLoader : HistoryDateLoader = { dateIndex ->
        getStationsInAllDates(1, dateIndex)
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




    fun cleanHistoryTab(){

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
        isTitleOneDateHeaderSet = false

    }


    // Bookmarked title

    val bookmarkedTitlesLivedata = repository.bookmarkedTitlesLiveData()

    fun deleteBookmarkTitle (title: BookmarkedTitle) = viewModelScope.launch {
        repository.deleteBookmarkTitle(title)
    }

    fun restoreBookmarkTitle (title: BookmarkedTitle) = viewModelScope.launch {
        repository.insertNewBookmarkedTitle(title)
    }

    fun upsertBookmarkedTitle(title : Title) = viewModelScope.launch {

        repository.deleteBookmarksByTitle(title.title)

        repository.insertNewBookmarkedTitle(
            BookmarkedTitle(
                timeStamp = System.currentTimeMillis(),
                date = title.date,
                title = title.title,
                stationName = title.stationName,
                stationIconUri = title.stationIconUri
            )
        )

        checkAndCleanBookmarkTitles()
    }


    fun checkAndCleanBookmarkTitles() = viewModelScope.launch {

        val count = repository.countBookmarkedTitles()

        if(count > RadioService.historyPrefBookmark && RadioService.historyPrefBookmark != 100){

            val bookmark = repository.getLastValidBookmarkedTitle(RadioService.historyPrefBookmark -1)

            repository.cleanBookmarkedTitles(bookmark.timeStamp)

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


    var isRecordingsCheckNeeded = true

    fun checkRecordingsForCleanUp(recList : List<Recording>){

        if(isRecordingsCheckNeeded){

            viewModelScope.launch(Dispatchers.IO){

                val fileList = app.fileList().filter {
                    it.endsWith(".ogg")
                }

                if(fileList.size > recList.size){

                    fileList.forEach { fileName ->

                        val rec = recList.find { rec ->
                            rec.id == fileName
                        }

                        if(rec == null){
                            app.deleteFile(fileName)
                        }
                    }
                }

                isRecordingsCheckNeeded = false
            }
        }
    }


    fun insertNewRecording(rec : Recording) =
        viewModelScope.launch {
            repository.insertRecording(rec)
        }

    fun deleteRecording(recId : String) = viewModelScope.launch {
        repository.deleteRecording(recId)
    }


     val allRecordingsLiveData = radioSource.allRecordingsLiveData







    fun removeRecordingFile(recordingID : String) = viewModelScope.launch(Dispatchers.IO) {
        app.deleteFile(recordingID)
    }


    fun renameRecording(id : String, newName: String) = viewModelScope.launch {
        repository.renameRecording(id, newName)
    }

}