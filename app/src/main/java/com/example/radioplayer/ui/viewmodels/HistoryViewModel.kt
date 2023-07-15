package com.example.radioplayer.ui.viewmodels

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.example.radioplayer.adapters.datasources.HistoryDataSource
import com.example.radioplayer.adapters.datasources.HistoryDateLoader
import com.example.radioplayer.adapters.datasources.HistoryOneDateLoader
import com.example.radioplayer.adapters.datasources.HistoryOneDateSource
import com.example.radioplayer.adapters.datasources.TitlesDataSource
import com.example.radioplayer.adapters.datasources.TitlesPageLoader
import com.example.radioplayer.adapters.models.StationWithDateModel
import com.example.radioplayer.adapters.models.TitleWithDateModel
import com.example.radioplayer.data.local.entities.BookmarkedTitle
import com.example.radioplayer.data.local.entities.Title
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.RadioServiceConnection
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.utils.Commands.COMMAND_UPDATE_HISTORY_MEDIA_ITEMS
import com.example.radioplayer.utils.Commands.COMMAND_UPDATE_HISTORY_ONE_DATE_MEDIA_ITEMS
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.sql.Date
import java.util.Calendar
import javax.inject.Inject


const val TAB_STATIONS = 0
const val TAB_TITLES = 1
const val TAB_BOOKMARKS = 2


@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: DatabaseRepository,
    private val radioSource: RadioSource,
    private val radioServiceConnection: RadioServiceConnection
) : ViewModel() {

    // Dates for RecyclerView

    val listOfDates = repository.getListOfDates

    var selectedDate = 0L

    private suspend fun getStationsInAllDates(limit: Int, offset: Int): List<StationWithDateModel> {

//        Log.d("CHECKTAGS", "callings all DATES get fun")

        val response = radioSource.getStationsInAllDates(limit, offset)

        if(RadioService.currentMediaItems == Constants.SEARCH_FROM_HISTORY){
            radioServiceConnection.sendCommand(
                COMMAND_UPDATE_HISTORY_MEDIA_ITEMS,
                bundleOf(Pair(Constants.IS_TO_CLEAR_HISTORY_ITEMS, offset == 0))
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

//        Log.d("CHECKTAGS", "callings one date get fun")

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

//        Log.d("CHECKTAGS", "callings all titles get fun")

        val response = repository.getTitlesPage(pageIndex * Constants.PAGE_SIZE, pageSize)
        val titlesWithDates: MutableList<TitleWithDateModel> = mutableListOf()

        response.forEach { title ->

            if(title.date != lastTitleDate){

                if(isTitleHeaderSet) {
                    titlesWithDates.add(TitleWithDateModel.TitleDateSeparatorEnclosing(dateToString))
                }
                calendar.time = Date(title.date)
                dateToString = Utils.fromDateToString(calendar)
                titlesWithDates.add(TitleWithDateModel.TitleDateSeparator(dateToString))
                isTitleHeaderSet = true
                lastTitleDate = title.date

            }
            titlesWithDates.add(TitleWithDateModel.TitleItem(title))
        }

        if(response.size < Constants.PAGE_SIZE && isTitleHeaderSet){
            titlesWithDates.add(TitleWithDateModel.TitleDateSeparatorEnclosing(dateToString))
            isTitleHeaderSet = false
        }

        return titlesWithDates
    }


    var isTitleOneDateHeaderSet = false
    var isTitleOneDateFooterSet = false

    private suspend fun getTitlesInOneDate(pageIndex : Int, pageSize : Int): List<TitleWithDateModel> {

//        Log.d("CHECKTAGS", "callings one title get fun")

        val response = repository.getTitlesInOneDatePage(pageIndex * Constants.PAGE_SIZE, pageSize, selectedDate)
        val titlesWithDates: MutableList<TitleWithDateModel> = mutableListOf()

        if(!isTitleOneDateHeaderSet){
            isTitleOneDateHeaderSet = true
            calendar.time = Date(selectedDate)
            titlesWithDates.add(
                TitleWithDateModel.TitleDateSeparator(
                    Utils.fromDateToString(
                        calendar
                    )
                ))
        }

        response.forEach { title ->
            titlesWithDates.add(TitleWithDateModel.TitleItem(title))
        }

        if(response.size < Constants.PAGE_SIZE){
            if(!isTitleOneDateFooterSet){
                isTitleOneDateFooterSet = true
                titlesWithDates.add(
                    TitleWithDateModel.TitleDateSeparatorEnclosing(
                        Utils.fromDateToString(
                            calendar
                        )
                    ))
            }
        }

        return titlesWithDates
    }



    private val _currentTab = MutableStateFlow(TAB_STATIONS)
    val currentTab = _currentTab.asStateFlow()

    private val selectedDateFlow = MutableStateFlow(0L)

    private var isInBookmarksFlow = false
    private var isHistoryInStationsTabFlow = true

    fun setIsInBookmarks() {
        isInBookmarksFlow = !isInBookmarksFlow
        if(isInBookmarksFlow)
            _currentTab.value = TAB_BOOKMARKS
        else
            _currentTab.value = TAB_TITLES
    }
    fun setIsInStations(value : Boolean) {
        isHistoryInStationsTabFlow = value
        if(value){
            _currentTab.value = TAB_STATIONS
        } else if(!isInBookmarksFlow){
            _currentTab.value = TAB_TITLES
        } else {
            _currentTab.value = TAB_BOOKMARKS
        }
    }

    fun updateSelectedDate(date : Long){
        selectedDate = date
        selectedDateFlow.value = date
    }


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


    private var historyFlow : Flow<PagingData<StationWithDateModel>>?  = null

    private var titlesFlow : Flow<PagingData<TitleWithDateModel>>? = null


    var observableHistoryPages : Flow<Any>? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun initiateHistory(){
        observableHistoryPages = _currentTab.combine(selectedDateFlow){tab, date ->
            tab
        }.flatMapLatest {tab ->
            if(tab == TAB_STATIONS){
                if(selectedDate == 0L)
                    historyFlow?: emptyFlow()
                else
                    Pager(
                        config = PagingConfig(
                            pageSize = 10,
                            enablePlaceholders = false
                        ),
                        pagingSourceFactory = {
                            HistoryOneDateSource(oneDateHistoryLoader)
                        }
                    ).flow

            } else if (tab == TAB_TITLES){
                if(selectedDate == 0L)
                    titlesFlow ?: emptyFlow()
                else {
                    isTitleOneDateHeaderSet = false
                    isTitleOneDateFooterSet = false
                    Pager(
                        config = PagingConfig(
                            pageSize = Constants.PAGE_SIZE,
                            initialLoadSize = Constants.PAGE_SIZE,
                            enablePlaceholders = false
                        ), pagingSourceFactory = {
                            TitlesDataSource(oneDateTitleLoader, Constants.PAGE_SIZE)
                        }
                    ).flow
                }
            }
            else
                bookmarkedTitlesLivedata
        }
    }


    fun setHistoryLiveData(){

        historyFlow = Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                HistoryDataSource(allHistoryLoader)
            }
        ).flow.cachedIn(viewModelScope)
    }


    fun setTitlesLiveData(){

        titlesFlow = Pager(
            config = PagingConfig(
                pageSize = Constants.PAGE_SIZE,
                initialLoadSize = Constants.PAGE_SIZE,
                enablePlaceholders = false
            ), pagingSourceFactory = {
                TitlesDataSource(allTitlesLoader, Constants.PAGE_SIZE)
            }
        ).flow.cachedIn(viewModelScope)
    }



//    fun updateBookmarkedState(isBookmarked : Boolean, title : String) = viewModelScope.launch{
//        repository.updateBookmarkedState(isBookmarked, title)
//    }



    fun cleanHistoryTab(){

        historyFlow = null
        titlesFlow = null

        observableHistoryPages = null
        lastTitleDate = 0L
        isTitleHeaderSet = false
        isTitleOneDateHeaderSet = false
        isTitleOneDateFooterSet = false
    }


    // Bookmarked title

    private val bookmarkedTitlesLivedata = repository.bookmarkedTitlesLiveData()

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
}