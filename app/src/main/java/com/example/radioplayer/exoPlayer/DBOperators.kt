package com.example.radioplayer.exoPlayer

import android.content.Context
import android.util.Log
import com.example.radioplayer.data.local.entities.BookmarkedTitle
import com.example.radioplayer.data.local.entities.HistoryDate
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Title
import com.example.radioplayer.data.local.relations.StationDateCrossRef
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
        service.databaseRepository
    }


    private val durationPref by lazy {
        service.getSharedPreferences(DURATION_UPDATE_PREF, Context.MODE_PRIVATE)
    }

    private val historySettingsPref by lazy{
        service.getSharedPreferences(Constants.HISTORY_PREF, Context.MODE_PRIVATE)
    }



    fun updateStationLastClicked(stationId : String) = serviceScope.launch(Dispatchers.IO){
        RadioService.isToUpdateLiveData = false
        databaseRepository.updateRadioStationLastClicked(stationId)
    }

    private var previousPlayedStationId = ""
    private var stationStartPlayingTime = 0L

    fun updateStationPlayedDuration() = serviceScope.launch(Dispatchers.IO){

        if(service.isPlaybackStatePlaying){
            if(previousPlayedStationId.isBlank()){
                previousPlayedStationId = service.currentRadioStation?.stationuuid ?: ""
                stationStartPlayingTime = System.currentTimeMillis()

                Log.d("CHECKTAGS", "duration start for id : $previousPlayedStationId")

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
            databaseRepository.updateRadioStationPlayedDuration(
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
            databaseRepository.updateRadioStationPlayedDuration(
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


     fun getLastDateAndCheck() = serviceScope.launch(Dispatchers.IO) {

        val date = databaseRepository.getLastDate()
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
            databaseRepository.insertNewDate(newDate)

            checkStationsAndReducePlayDurationIfNeeded()

        }
        databaseRepository.insertStationDateCrossRef(StationDateCrossRef(stationID, RadioService.currentDateString))

    }



    private suspend fun checkStationsAndReducePlayDurationIfNeeded(){

        val stations = databaseRepository.getStationsForDurationCheck()

        val time = System.currentTimeMillis()

        stations.forEach {
            if(time - it.lastClick > PLAY_DURATION_MILLS_CHECK)
                databaseRepository.updateStationPlayDuration(it.playDuration / 2, it.stationuuid)
        }
    }

    private suspend fun compareDatesWithPrefAndCLeanIfNeeded() {

        val numberOfDatesInDB = databaseRepository.getNumberOfDates()

        val historyDatesPref = historySettingsPref.getInt(
            Constants.HISTORY_PREF_DATES, Constants.HISTORY_DATES_PREF_DEFAULT
        )

        if(historyDatesPref < numberOfDatesInDB) {

            val numberOfDatesToDelete = numberOfDatesInDB - historyDatesPref
            val deleteList = databaseRepository.getDatesToDelete(numberOfDatesToDelete)

             CoroutineScope(Dispatchers.IO).launch {

                deleteList.forEach {

                    launch {
                        databaseRepository.deleteAllCrossRefWithDate(it.date)
                        databaseRepository.deleteDate(it)
                        databaseRepository.deleteTitlesWithDate(it.time)
                    }
                }
            }.join()

            val stations = databaseRepository.gatherStationsForCleaning()

            CoroutineScope(Dispatchers.IO).launch {

                stations.forEach {

                    launch {
                        val checkIfInHistory = databaseRepository.checkIfRadioStationInHistory(it.stationuuid)

                        if(!checkIfInHistory){

                            databaseRepository.deleteRadioStation(it)
                        }
                    }
                }
            }
        }
    }


     fun upsertNewBookmark() = CoroutineScope(Dispatchers.IO).launch{

        if(RadioService.currentlyPlayingSong != Constants.TITLE_UNKNOWN){

            databaseRepository.deleteBookmarksByTitle(RadioService.currentlyPlayingSong)

            databaseRepository.insertNewBookmarkedTitle(
                BookmarkedTitle(
                    timeStamp = System.currentTimeMillis(),
                    date = RadioService.currentDateLong,
                    title = RadioService.currentlyPlayingSong,
                    stationName = service.currentRadioStation?.name ?: "",
                    stationIconUri = service.currentRadioStation?.favicon ?: ""
                )
            )

            val count = databaseRepository.countBookmarkedTitles()

            if(count > RadioService.historyPrefBookmark && RadioService.historyPrefBookmark != 100){

                val bookmark = databaseRepository.getLastValidBookmarkedTitle(RadioService.historyPrefBookmark -1)

                databaseRepository.cleanBookmarkedTitles(bookmark.timeStamp)
            }
        }
    }


    fun insertNewTitle(title: String){

        serviceScope.launch(Dispatchers.IO){

            val checkTitle = databaseRepository.checkTitleTimestamp(title, RadioService.currentDateLong)

            val isTitleBookmarked = checkTitle?.isBookmarked

            checkTitle?.let {
                databaseRepository.deleteTitle(it)
            }
            val stationName = service.currentRadioStation?.name ?: ""

            val stationUri = service.currentRadioStation?.favicon ?: ""


            databaseRepository.insertNewTitle(
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