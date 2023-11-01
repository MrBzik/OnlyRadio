package com.onlyradio.radioplayer.exoPlayer

import android.content.Context
import com.onlyradio.radioplayer.data.local.entities.BookmarkedTitle
import com.onlyradio.radioplayer.data.local.entities.HistoryDate
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.data.local.entities.Title
import com.onlyradio.radioplayer.data.local.relations.StationDateCrossRef
import com.onlyradio.radioplayer.utils.Constants
import com.onlyradio.radioplayer.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Date

private const val PLAY_DURATION_MILLS_CHECK = 604800000

private const val DURATION_UPDATE_PREF = "duration update recovery shared pref"
private const val LAST_PLAYED_STATION_ID = "last played station Id"
private const val LAST_PLAYED_STATION_DURATION = "last played station duration"

class DBOperators (private val service: RadioService) {

    private val serviceScope by lazy {
        service.serviceScope
    }

    private val databaseRepository by lazy {
        service.favRepo
    }

    private val bookmarksRepo by lazy {
        service.bookmarksRepo
    }

    private val titlesRepo by lazy {
        service.titlesRepo
    }

    private val datesRepo by lazy {
        service.datesRepo
    }

    private val lazyRepo by lazy {
        service.lazyRepo
    }


    private val durationPref by lazy {
        service.getSharedPreferences(DURATION_UPDATE_PREF, Context.MODE_PRIVATE)
    }

    private val historySettingsPref by lazy{
        service.getSharedPreferences(Constants.HISTORY_PREF, Context.MODE_PRIVATE)
    }

    private var previousPlayedStationId = ""
    private var stationStartPlayingTime = 0L


    fun updateStationLastClicked(stationId : String) = serviceScope.launch(Dispatchers.IO){
        lazyRepo.updateRadioStationLastClicked(stationId)
    }

    fun updateStationPlayedDuration() = serviceScope.launch(Dispatchers.IO){

        if(service.isPlaybackStatePlaying){
            if(previousPlayedStationId.isBlank()){
                previousPlayedStationId = service.currentRadioStation?.stationuuid ?: ""
                stationStartPlayingTime = System.currentTimeMillis()

//                Log.d("CHECKTAGS", "duration start for id : $previousPlayedStationId")

            } else {
                durationUpdateHelper()
                stationStartPlayingTime = System.currentTimeMillis()
                previousPlayedStationId = service.currentRadioStation?.stationuuid ?: ""
            }

        } else {
            durationUpdateHelper()
            previousPlayedStationId = ""
        }
    }
    private suspend fun durationUpdateHelper(){
        val duration = System.currentTimeMillis() - stationStartPlayingTime
        if(duration > 20000 && previousPlayedStationId.isNotBlank()){
            lazyRepo.updateRadioStationPlayedDuration(
                previousPlayedStationId, duration
            )
        }
    }

    fun savePlayDurationOnServiceKill(isPlaybackStatePlaying : Boolean){
        if(isPlaybackStatePlaying){
            val duration = System.currentTimeMillis() - stationStartPlayingTime
            if(duration > 20000 && previousPlayedStationId.isNotBlank()){
                durationPref.edit().apply {
                    putString(LAST_PLAYED_STATION_ID, previousPlayedStationId)
                    putLong(LAST_PLAYED_STATION_DURATION, duration)
                }.commit()
            }
        }
    }

    private suspend fun recoverAndUpdateLastPlayDuration(){
        val lastStationId = durationPref.getString(LAST_PLAYED_STATION_ID, "") ?: ""
        if(lastStationId.isNotBlank()){
            val duration = durationPref.getLong(LAST_PLAYED_STATION_DURATION, 0)
            lazyRepo.updateRadioStationPlayedDuration(
                lastStationId, duration
            )
            durationPref.edit().putString(LAST_PLAYED_STATION_ID, "").apply()
        }
    }

    fun insertRadioStation(station : RadioStation) = serviceScope.launch(Dispatchers.IO){

        databaseRepository.insertRadioStation(station)

    }

     fun initialHistoryPref(){
        RadioService.historyPrefBookmark = historySettingsPref.getInt(
            Constants.HISTORY_PREF_BOOKMARK,
            Constants.HISTORY_BOOKMARK_PREF_DEFAULT
        )
    }


    var isLastDateUpToDate = true


//    private suspend fun testClickOnHistory(){
//        val lastDate = databaseRepository.getLastDate()
//        lastDate?.let {
//            databaseRepository.deleteDate(it)
//            databaseRepository.deleteAllCrossRefWithDate(it.date)
//            databaseRepository.deleteTitlesWithDate(it.time)
//        }
//    }


     fun getLastDateAndCheck() = serviceScope.launch {

//         testClickOnHistory()


        val date = datesRepo.getLastDate()
        date?.let {
            RadioService.currentDateString = it.date
            RadioService.currentDateLong = it.time
        }

        val newTime = System.currentTimeMillis()
        service.calendar.time = Date(newTime)
        val update = Utils.fromDateToString(service.calendar)

        if(update != RadioService.currentDateString){
            RadioService.currentDateString = update
            RadioService.currentDateLong = newTime
            isLastDateUpToDate = false
        }

        recoverAndUpdateLastPlayDuration()

        compareDatesWithPrefAndCLeanIfNeeded()

    }


    fun checkDateAndUpdateHistory(stationID: String) = serviceScope.launch(Dispatchers.IO) {

        if(!isLastDateUpToDate){
            isLastDateUpToDate = true
            val newDate = HistoryDate(RadioService.currentDateString, RadioService.currentDateLong)
            datesRepo.insertNewDate(newDate)

            checkStationsAndReducePlayDurationIfNeeded()

        }
        datesRepo.insertStationDateCrossRef(StationDateCrossRef(stationID, RadioService.currentDateString))

    }



    private suspend fun checkStationsAndReducePlayDurationIfNeeded(){

        val stations = lazyRepo.getStationsForDurationCheck()

        val time = System.currentTimeMillis()

        stations.forEach {
            if(time - it.lastClick > PLAY_DURATION_MILLS_CHECK)
                lazyRepo.updateStationPlayDuration(it.playDuration / 2, it.stationuuid)
        }
    }

    private suspend fun compareDatesWithPrefAndCLeanIfNeeded() {

        val numberOfDatesInDB = datesRepo.getNumberOfDates()

        val historyDatesPref = historySettingsPref.getInt(
            Constants.HISTORY_PREF_DATES, Constants.HISTORY_DATES_PREF_DEFAULT
        )

        if(historyDatesPref < numberOfDatesInDB) {

            val numberOfDatesToDelete = numberOfDatesInDB - historyDatesPref
            val deleteList = datesRepo.getDatesToDelete(numberOfDatesToDelete)

             serviceScope.launch(Dispatchers.IO) {

                deleteList.forEach {

                    launch {
                        datesRepo.deleteAllCrossRefWithDate(it.date)
                        datesRepo.deleteDate(it)
                        titlesRepo.deleteTitlesWithDate(it.time)
                    }
                }
            }.join()

            val stations = datesRepo.gatherStationsForCleaning()

            serviceScope.launch(Dispatchers.IO) {

                stations.forEach {

                    launch {
                        val checkIfInHistory = datesRepo.checkIfRadioStationInHistory(it.stationuuid)

                        if(!checkIfInHistory){

                            datesRepo.deleteRadioStation(it)
                        }
                    }
                }
            }
        }
    }


     fun upsertNewBookmark() = serviceScope.launch(Dispatchers.IO) {

        if(RadioService.currentlyPlayingSong != Constants.TITLE_UNKNOWN){

            bookmarksRepo.deleteBookmarksByTitle(RadioService.currentlyPlayingSong)

            bookmarksRepo.insertNewBookmarkedTitle(
                BookmarkedTitle(
                    timeStamp = System.currentTimeMillis(),
                    date = RadioService.currentDateLong,
                    title = RadioService.currentlyPlayingSong,
                    stationName = service.currentRadioStation?.name ?: "",
                    stationIconUri = service.currentRadioStation?.favicon ?: ""
                )
            )

            val count = bookmarksRepo.countBookmarkedTitles()

            if(count > RadioService.historyPrefBookmark && RadioService.historyPrefBookmark != 100){

                val bookmark = bookmarksRepo.getLastValidBookmarkedTitle(RadioService.historyPrefBookmark -1)

                bookmarksRepo.cleanBookmarkedTitles(bookmark.timeStamp)
            }
        }
    }


    fun insertNewTitle(title: String){

        serviceScope.launch(Dispatchers.IO){

            val checkTitle = titlesRepo.checkTitleTimestamp(title, RadioService.currentDateLong)

            val isTitleBookmarked = checkTitle?.isBookmarked

            checkTitle?.let {
                titlesRepo.deleteTitle(it)
            }
            val stationName = service.currentRadioStation?.name ?: ""

            val stationUri = service.currentRadioStation?.favicon ?: ""


            titlesRepo.insertNewTitle(
                Title(
                timeStamp = System.currentTimeMillis(),
                date = RadioService.currentDateLong,
                title = title,
                stationName = stationName,
                stationIconUri = stationUri,
                isBookmarked = isTitleBookmarked ?: false
                )
            )

            service.lastInsertedSong = title

        }
    }



}