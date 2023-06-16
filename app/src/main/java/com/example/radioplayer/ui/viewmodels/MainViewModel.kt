package com.example.radioplayer.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import androidx.paging.*
import com.example.radioplayer.RadioApplication
import com.example.radioplayer.adapters.datasources.RadioStationsDataSource
import com.example.radioplayer.adapters.datasources.StationsPageLoader
import com.example.radioplayer.adapters.models.CountryWithRegion
import com.example.radioplayer.connectivityObserver.ConnectivityObserver
import com.example.radioplayer.connectivityObserver.NetworkConnectivityObserver
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.data.remote.entities.RadioStations
import com.example.radioplayer.exoPlayer.*
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.ui.dialogs.*
import com.example.radioplayer.ui.fragments.RadioSearchFragment.Companion.listOfCountries
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.COMMAND_ADD_MEDIA_ITEM
import com.example.radioplayer.utils.Constants.COMMAND_CHANGE_BASS_LEVEL
import com.example.radioplayer.utils.Constants.COMMAND_CHANGE_REVERB_MODE
import com.example.radioplayer.utils.Constants.COMMAND_CLEAR_MEDIA_ITEMS

import com.example.radioplayer.utils.Constants.COMMAND_NEW_SEARCH
import com.example.radioplayer.utils.Constants.COMMAND_START_RECORDING
import com.example.radioplayer.utils.Constants.COMMAND_STOP_RECORDING

import com.example.radioplayer.utils.Constants.COMMAND_REMOVE_RECORDING_MEDIA_ITEM
import com.example.radioplayer.utils.Constants.COMMAND_REMOVE_MEDIA_ITEM
import com.example.radioplayer.utils.Constants.COMMAND_RESTART_PLAYER
import com.example.radioplayer.utils.Constants.COMMAND_RESTORE_RECORDING_MEDIA_ITEM
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_FAV_PLAYLIST
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_HISTORY
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_RADIO_PLAYBACK_PITCH
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_RADIO_PLAYBACK_SPEED
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_REC_PLAYBACK_SPEED
import com.example.radioplayer.utils.Constants.FAB_POSITION_X
import com.example.radioplayer.utils.Constants.FAB_POSITION_Y
import com.example.radioplayer.utils.Constants.IS_CHANGE_MEDIA_ITEMS
import com.example.radioplayer.utils.Constants.IS_FAB_UPDATED
import com.example.radioplayer.utils.Constants.IS_NAME_EXACT
import com.example.radioplayer.utils.Constants.IS_NEW_SEARCH
import com.example.radioplayer.utils.Constants.IS_TAG_EXACT
import com.example.radioplayer.utils.Constants.ITEM_ID
import com.example.radioplayer.utils.Constants.ITEM_INDEX
import com.example.radioplayer.utils.Constants.PAGE_SIZE
import com.example.radioplayer.utils.Constants.PLAY_WHEN_READY
import com.example.radioplayer.utils.Constants.SEARCH_BTN_PREF

import com.example.radioplayer.utils.Constants.SEARCH_FLAG
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.example.radioplayer.utils.Constants.SEARCH_FULL_COUNTRY_NAME
import com.example.radioplayer.utils.Constants.SEARCH_PREF_COUNTRY
import com.example.radioplayer.utils.Constants.SEARCH_PREF_FULL_AUTO
import com.example.radioplayer.utils.Constants.SEARCH_PREF_MAX_BIT
import com.example.radioplayer.utils.Constants.SEARCH_PREF_MIN_BIT
import com.example.radioplayer.utils.Constants.SEARCH_PREF_NAME
import com.example.radioplayer.utils.Constants.SEARCH_PREF_NAME_AUTO
import com.example.radioplayer.utils.Constants.SEARCH_PREF_ORDER
import com.example.radioplayer.utils.Constants.SEARCH_PREF_TAG
import com.example.radioplayer.utils.Constants.TEXT_SIZE_STATION_TITLE_PREF
import com.example.radioplayer.utils.Language
import com.example.radioplayer.utils.listOfLanguages
import com.example.radioplayer.utils.toRadioStation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@HiltViewModel
class MainViewModel @Inject constructor(
    app : Application,
    private val radioServiceConnection: RadioServiceConnection,
    val radioSource: RadioSource,
    private val repository: DatabaseRepository
) : AndroidViewModel(app) {

//       val isConnected = radioServiceConnection.isConnected
       val currentRadioStation = radioServiceConnection.currentRadioStation
       val networkError = radioServiceConnection.networkError
       val playbackState = radioServiceConnection.playbackState

       val textSizePref = app.getSharedPreferences(TEXT_SIZE_STATION_TITLE_PREF, Context.MODE_PRIVATE)

       var stationsTitleSize = textSizePref.getFloat(TEXT_SIZE_STATION_TITLE_PREF, 20f)

//       private var listOfStations = listOf<RadioStation>()
       var isNewSearch = true

//       val newPlayingItem : MutableLiveData<PlayingItem> = MutableLiveData()


       var isInSettingsExtras = false


       var isInDetailsFragment = false
       var currentFragment = 0


       var noResultDetection : MutableLiveData<Boolean> = MutableLiveData()

       val currentSongTitle = RadioService.currentSongTitle

       var isInitialLaunchOfTheApp = true

       var isSmoothTransitionNeeded = false


       val isPlayerBuffering = radioSource.isPlayerBuffering


//     fun disconnectMediaBrowser(){
//            radioServiceConnection.sendCommand(COMMAND_STOP_SERVICE, null)
//         radioServiceConnection.disconnectBrowser()
//    }

    fun connectMediaBrowser(){
        radioServiceConnection.connectBrowser()
    }

//    init {
//
//        currentRadioStation.value?.let {
//            viewModelScope.launch {
//                val itemId = it.getString(METADATA_KEY_MEDIA_ID)
//
//                if(itemId != null){
//
//                    if(itemId.contains(".ogg")) {
//                        val item = repository.getCurrentRecording(itemId)
//                        newPlayingItem.postValue(PlayingItem.FromRecordings(item))
//                        isRadioTrueRecordingFalse = false
//                    } else {
//                        val item = repository.getCurrentRadioStation(itemId)
//                        newPlayingItem.postValue(PlayingItem.FromRadio(item))
//                        isRadioTrueRecordingFalse = true
//
//                    }
//
//                }
//
//
//            }
//        }
//
//    }


    val connectivityObserver = NetworkConnectivityObserver(getApplication())

    var hasInternetConnection : MutableLiveData<Boolean> = MutableLiveData(
        connectivityObserver.isNetworkAvailable()
    )

    private fun setConnectivityObserver() {

        connectivityObserver.observe().onEach {

            when (it) {
                ConnectivityObserver.Status.Available -> {
                    hasInternetConnection.postValue(true)

//                    if(wasSearchInterrupted){
//                        wasSearchInterrupted = false
//                        searchBy.postValue(true)
//                    }
                }
                ConnectivityObserver.Status.Unavailable -> {
                    hasInternetConnection.postValue(false)
                }
                ConnectivityObserver.Status.Lost -> {
                    hasInternetConnection.postValue(false)
                }
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    init {
        setConnectivityObserver()
    }

       var isCountryListToUpdate = true


       val searchPreferences = app.getSharedPreferences("SearchPref", Context.MODE_PRIVATE)

       val searchParamTag : MutableLiveData<String> = MutableLiveData()
       val searchParamName : MutableLiveData<String> = MutableLiveData()
       val searchParamCountry : MutableLiveData<String> = MutableLiveData()

       var isNameAutoSearch = searchPreferences.getBoolean(SEARCH_PREF_NAME_AUTO, true)
       var isFullAutoSearch = searchPreferences.getBoolean(SEARCH_PREF_FULL_AUTO, true)

       var lastSearchCountry = searchPreferences.getString(SEARCH_PREF_COUNTRY, "") ?: ""
       var lastSearchName = searchPreferences.getString(SEARCH_PREF_NAME, "")?: ""
       var lastSearchTag = searchPreferences.getString(SEARCH_PREF_TAG, "")?: ""
       var searchFullCountryName = searchPreferences.getString(SEARCH_FULL_COUNTRY_NAME, "")?: ""


       var isTagExact = searchPreferences.getBoolean(IS_TAG_EXACT, false)
       var isNameExact = searchPreferences.getBoolean(IS_NAME_EXACT, false)
       var wasTagExact = isTagExact
       var wasNameExact = isNameExact

       var oldSearchOrder = searchPreferences.getString(SEARCH_PREF_ORDER, ORDER_POP) ?: ORDER_POP
       var newSearchOrder = oldSearchOrder

       var minBitrateOld = searchPreferences.getInt(SEARCH_PREF_MIN_BIT, BITRATE_0)
       var minBitrateNew = minBitrateOld

       var maxBitrateOld = searchPreferences.getInt(SEARCH_PREF_MAX_BIT, BITRATE_MAX)
       var maxBitrateNew = maxBitrateOld

       var isSearchFilterLanguage = searchPreferences.getBoolean(Constants.IS_SEARCH_FILTER_LANGUAGE, false)
       var wasSearchFilterLanguage = isSearchFilterLanguage



    fun updateCountryList(handleResults : () -> Unit) = viewModelScope.launch {

        if(isCountryListToUpdate){

            try {
                val countryList = radioSource.getAllCountries()
                if(countryList.isSuccessful){
                    countryList.body()?.let {
                        it.forEach { country ->

                            for(i in listOfCountries.indices){
                                if(listOfCountries[i] is CountryWithRegion.Country){
                                    if((listOfCountries[i] as CountryWithRegion.Country)
                                            .countryCode == country.iso_3166_1){
                                        (listOfCountries[i] as CountryWithRegion.Country).stationsCount =
                                            country.stationcount
                                        break
                                    }
                                }
                            }
                        }

                        handleResults()

                        isCountryListToUpdate = false
                    }

                }


            } catch (e : Exception){ }

        }
    }

    private var currentLanguage = ""

    private var stationsCountForLanguage = 0

    fun updateLanguageCount(resultHandler : (Int) -> Unit) = viewModelScope.launch {

        val newLang = Locale.getDefault().language

        if(currentLanguage != newLang){

            var language : Language? = null

            for(i in listOfLanguages.indices){
                if(listOfLanguages[i].iso == newLang){
                    language = listOfLanguages[i]
                    break
                }
            }

            resultHandler(language?.stationCount ?: 0)
            stationsCountForLanguage

             language?.let {

                try {
                    val response = radioSource.getLanguages(language.name)
                      response.body()?.let { langs ->
                        var count = 0
                        langs.forEach {
                            count += it.stationcount
                        }
                        resultHandler(count)
                        stationsCountForLanguage = count
                        currentLanguage = newLang
                    }

                } catch (e : Exception){}
            }
        } else {
            resultHandler(stationsCountForLanguage)
        }
    }


    private val searchBy : MutableLiveData<Boolean> = MutableLiveData()


    @OptIn(ExperimentalCoroutinesApi::class)
    val stationsFlow = searchBy.asFlow()
        .flatMapLatest {
            if(it)
            searchStationsPaging()
            else {
                searchBy.postValue(true)

//                if(hasInternetConnection.value == true){
//
//                } else {
//                    wasSearchInterrupted = true
//                }
                flowOf(PagingData.empty())
            }
        }
        .cachedIn(viewModelScope)

//     var wasSearchInterrupted = false


       init {
           searchParamTag.postValue(lastSearchTag)
           searchParamName.postValue(lastSearchName)
           searchParamCountry.postValue(lastSearchCountry)
           searchBy.postValue(true)
       }


       var fabX = 0f
       var fabY = 0f

       var fabPref: SharedPreferences = app.getSharedPreferences(SEARCH_BTN_PREF, Context.MODE_PRIVATE)
       var isFabMoved = fabPref.getBoolean(IS_FAB_UPDATED, false)

       var isFabUpdated = false
       init {
            if(isFabMoved){
                fabX = fabPref.getFloat(FAB_POSITION_X, 0f)
                fabY = fabPref.getFloat(FAB_POSITION_Y, 0f)
            }
        }



//       var isWaitingForNewPage = false
//       var isWaitingForNewSearch = false
//       val isServerNotResponding : MutableLiveData<Boolean> = MutableLiveData(false)
//

       val searchLoadingState : MutableLiveData<Boolean> = MutableLiveData()


        var searchJob : Job = SupervisorJob()



       private suspend fun searchWithNewParams(

            limit : Int, offset : Int) : List<RadioStation> {

            searchLoadingState.postValue(true)

           Log.d("CHECKTAGS", "is new search? $isNewSearch")

               val calcOffset = limit * offset

//                var isReversedOrder = true


                val orderSetting = when(newSearchOrder){
                    ORDER_VOTES -> "votes"
                    ORDER_POP -> "clickcount"
                    ORDER_TREND -> "clicktrend"

//                    ORDER_BIT_MIN -> {
//                        isReversedOrder = false
//                        "bitrate"
//                    }
//                    ORDER_BIT_MAX -> "bitrate"
                    else -> "random"
                }

           val lang = if(isSearchFilterLanguage) Locale.getDefault().isO3Language
                        else ""

           var response : RadioStations? = null
           var listOfStations = emptyList<RadioStation>()

           searchJob = viewModelScope.launch {

               while(true){

//                   Log.d("CHECKTAGS", "search is looping")

                   response = radioSource.getRadioStationsSource(
                       offset = calcOffset,
                       pageSize = limit,
                       country = lastSearchCountry,
                       language = lang,
                       tag = lastSearchTag,
                       isTagExact = isTagExact,
                       name = lastSearchName,
                       isNameExact = isNameExact,
                       order = orderSetting,
                       minBit = minBitrateNew,
                       maxBit = maxBitrateNew,

                       )

                   if(response == null) {

                    hasInternetConnection.postValue(false)

                       delay(1000)
                   }

                   else break
               }


               hasInternetConnection.postValue(true)

               if(isNewSearch && response?.size == 0){
                   noResultDetection.postValue(true)
               } else {
                   noResultDetection.postValue(false)
               }

               listOfStations = response?.let {

                   it.map { station ->

                       station.toRadioStation()
                   }
               } ?: emptyList()

               while(!RadioServiceConnection.isConnected){
                   Log.d("CHECKTAGS", "not connected")
                   delay(50)
               }

               radioServiceConnection.sendCommand(COMMAND_NEW_SEARCH,
                   bundleOf(Pair(IS_NEW_SEARCH, isNewSearch)))

               isNewSearch = false

               searchLoadingState.postValue(false)
           }

           searchJob.join()

           return listOfStations

        }



    private fun searchStationsPaging(): Flow<PagingData<RadioStation>> {
        val loader : StationsPageLoader = { pageIndex, pageSize ->
            searchWithNewParams(pageSize, pageIndex)
        }

        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                RadioStationsDataSource(loader, PAGE_SIZE)
            }
        ).flow
    }



    fun initiateNewSearch() : Boolean {


        if(
            lastSearchName == searchParamName.value &&
            lastSearchTag == searchParamTag.value &&
            lastSearchCountry == searchParamCountry.value &&
            wasTagExact == isTagExact &&
            wasNameExact == isNameExact &&
            newSearchOrder == oldSearchOrder &&
            minBitrateNew == minBitrateOld &&
            maxBitrateNew == maxBitrateOld &&
            isSearchFilterLanguage == wasSearchFilterLanguage

        )
            return false


            return true.also {
                isNewSearch = true
                lastSearchName = searchParamName.value ?: ""
                lastSearchTag = searchParamTag.value ?: ""
                lastSearchCountry = searchParamCountry.value ?: ""
                wasTagExact = isTagExact
                wasNameExact = isNameExact
                oldSearchOrder = newSearchOrder
                minBitrateOld = minBitrateNew
                maxBitrateOld = maxBitrateNew
                wasSearchFilterLanguage = isSearchFilterLanguage

                if(searchJob.isActive){
                    searchJob.cancel()
                }


                searchBy.postValue(false)

            }
    }

        fun changeReverbMode(){
            radioServiceConnection.sendCommand(COMMAND_CHANGE_REVERB_MODE, null)
        }

        fun changeVirtualizerLevel(){
            radioServiceConnection.sendCommand(COMMAND_CHANGE_BASS_LEVEL, null)
        }


        fun restartPlayer(){
            radioServiceConnection.sendCommand(COMMAND_RESTART_PLAYER, null)
        }


        fun seekTo(position : Long){
            radioServiceConnection.transportControls.seekTo(position)

        }

        fun removeRecordingMediaItem(index : Int){

            radioServiceConnection.sendCommand(COMMAND_REMOVE_RECORDING_MEDIA_ITEM,
                bundleOf(Pair(ITEM_INDEX, index)))
        }

        fun restoreRecordingMediaItem(index : Int){
            radioServiceConnection.sendCommand(COMMAND_RESTORE_RECORDING_MEDIA_ITEM,
                bundleOf(Pair(ITEM_INDEX, index)))
        }


        fun playOrToggleRecording(
            rec : Recording,
            playWhenReady : Boolean = true,
            itemIndex : Int? = -1
            ): Boolean {

            val isToChangeMediaItems = RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS

            val isPrepared = playbackState.value?.isPrepared ?: false

            val id = rec.id

            if(isPrepared && id == RadioService.currentPlayingRecording.value?.id
                && RadioService.currentMediaItems == SEARCH_FROM_RECORDINGS
            ) {
                playbackState.value?.let { playbackState ->
                    when {
                        playbackState.isPlaying -> {

                            radioServiceConnection.transportControls.pause()
                            return false
                        }

                        playbackState.isPlayEnabled -> {

                            radioServiceConnection.transportControls.play()
                            return true
                        }
                        else -> false
                    }
                }

            } else {

                RadioService.currentMediaItems = SEARCH_FROM_RECORDINGS
                RadioService.recordingPlaybackPosition.postValue(0)

                radioServiceConnection.transportControls
                    .playFromMediaId(id, bundleOf(
                        Pair(SEARCH_FLAG, SEARCH_FROM_RECORDINGS),
                        Pair(PLAY_WHEN_READY, playWhenReady),
                        Pair(ITEM_INDEX, itemIndex),
                        Pair(IS_CHANGE_MEDIA_ITEMS, isToChangeMediaItems)
                    ))
            }

            return false
        }


        fun playOrToggleStation(
            station : RadioStation? = null,
            searchFlag : Int = 0,
            playWhenReady : Boolean = true,
            itemIndex : Int = -1,
//            historyItemId : String? = null,
            isToChangeMediaItems : Boolean
        ) : Boolean {



            val isPrepared = playbackState.value?.isPrepared ?: false

            val id = station?.stationuuid

            if(isPrepared && id == RadioService.currentPlayingStation.value?.stationuuid
                && RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS
                    ){

                RadioService.currentMediaItems = searchFlag

                var isToPlay = false

                playbackState.value?.let { playbackState ->
                    when {
                        playbackState.isPlaying -> {

                            if(isToChangeMediaItems) isToPlay = false
                            else
                            radioServiceConnection.transportControls.pause()
                        }

                        playbackState.isPlayEnabled -> {
                            if(isToChangeMediaItems) isToPlay = true
                            else
                            radioServiceConnection.transportControls.play()
                        }
                    }
                }

                if(isToChangeMediaItems){

                    radioServiceConnection.transportControls
                        .playFromMediaId(id, bundleOf(
                            Pair(SEARCH_FLAG, searchFlag),
                            Pair(PLAY_WHEN_READY, isToPlay),
                            Pair(ITEM_INDEX, itemIndex),
                            Pair(IS_CHANGE_MEDIA_ITEMS, true)
                        ))
                }

                return false
            } else {

                id?.let {
                    radioServiceConnection.sendCommand(COMMAND_UPDATE_HISTORY,
                    bundleOf(Pair(ITEM_ID, it))
                    )
                }


                RadioService.currentMediaItems = searchFlag
                radioServiceConnection.transportControls
                    .playFromMediaId(id, bundleOf(
                        Pair(SEARCH_FLAG, searchFlag),
                        Pair(PLAY_WHEN_READY, playWhenReady),
                        Pair(ITEM_INDEX, itemIndex),
                        Pair(IS_CHANGE_MEDIA_ITEMS, isToChangeMediaItems)
                    ))

                return true
            }


        }

//        var isRadioTrueRecordingFalse = true


        fun updateRadioPlaybackSpeed(){
            radioServiceConnection.sendCommand(COMMAND_UPDATE_RADIO_PLAYBACK_SPEED, null)
        }

        fun updateRadioPlaybackPitch(){
            radioServiceConnection.sendCommand(COMMAND_UPDATE_RADIO_PLAYBACK_PITCH, null)
        }

//        fun compareDatesWithPrefAndCLeanIfNeeded() {
//            radioServiceConnection.sendCommand(COMMAND_COMPARE_DATES_PREF_AND_CLEAN, null)
//         }

        fun updateFavPlaylist(){
            radioServiceConnection.sendCommand(COMMAND_UPDATE_FAV_PLAYLIST, null)
        }

        fun clearMediaItems(){
            radioServiceConnection.sendCommand(COMMAND_CLEAR_MEDIA_ITEMS, null)
        }


        fun removeMediaItem(index : Int){
            radioServiceConnection.sendCommand(COMMAND_REMOVE_MEDIA_ITEM,
                bundleOf(Pair(ITEM_INDEX, index)))
        }

        fun restoreMediaItem(index : Int){
            radioServiceConnection.sendCommand(COMMAND_ADD_MEDIA_ITEM,
                bundleOf(Pair(ITEM_INDEX, index)))
        }


    // ExoRecord

        val currentPlayerPosition = RadioService.recordingPlaybackPosition
        val currentRecordingDuration = RadioService.recordingDuration

        fun startRecording() {
            radioServiceConnection.sendCommand(COMMAND_START_RECORDING, null)
        }

        fun stopRecording(){
            radioServiceConnection.sendCommand(COMMAND_STOP_RECORDING, null)
        }
        val exoRecordFinishConverting = radioSource.exoRecordFinishConverting
        val exoRecordState = radioSource.exoRecordState
        val exoRecordTimer = radioSource.exoRecordTimer

//        val isRecordingUpdated = radioSource.isRecordingUpdated


        fun updateRecPlaybackSpeed(){
            radioServiceConnection.sendCommand(COMMAND_UPDATE_REC_PLAYBACK_SPEED, null)
        }

        var isCutExpanded = false


//        private fun calculateTags() = viewModelScope.launch{
//
//            val response = radioSource.getAllTags()
//
//        val builder = StringBuilder()
//        val builder2 = StringBuilder()
//        val builder3 = StringBuilder()
//        val builder4 = StringBuilder()
//        val builder5 = StringBuilder()
//        val builder6 = StringBuilder()
//        val builder7 = StringBuilder()
//        val builder8 = StringBuilder()
//        val builder9 = StringBuilder()
//        val builder10 = StringBuilder()
//
//
//            var buildCount = 0
//
//            for(i in RadioSearchFragment.tagsList.indices){
//
//                if(RadioSearchFragment.tagsList[i] is TagWithGenre.Tag){
//
//                    val tagItem = RadioSearchFragment.tagsList[i] as TagWithGenre.Tag
//
//                    var count = 0
//                    var countExact = 0
//
//                    response.body()?.forEach {
//
//                        if(it.name.contains(tagItem.tag)){
//                            count += it.stationcount
//                        }
//
//                        if(it.name == tagItem.tag){
//                            countExact += it.stationcount
//                        }
//                    }
//
//                   val newTag ="TagWithGenre.Tag(\"${tagItem.tag}\", $count, $countExact), "
//
//                  if(buildCount < 50) {
//                                    buildCount++
//                builder.append(
//                   newTag
//                )
//            } else if(buildCount < 100) {
//                                    buildCount++
//            builder2.append(
//                newTag
//            )
//        } else if(buildCount < 150) {
//                                    buildCount++
//                builder3.append(
//                    newTag
//                )
//            } else if(buildCount < 200) {
//                                    buildCount++
//                builder4.append(
//                    newTag
//                )
//            } else if(buildCount < 250) {
//                 buildCount++
//                builder5.append(newTag)
//
//            } else if(buildCount < 300) {
//                      buildCount++
//                      builder6.append(
//                          newTag
//                      )
//                  } else if(buildCount < 350) {
//                      buildCount++
//                      builder7.append(
//                          newTag
//                      )
//                  } else if(buildCount < 400) {
//                      buildCount++
//                      builder8.append(
//                          newTag
//                      )
//                  } else if(buildCount < 450) {
//                      buildCount++
//                      builder9.append(
//                          newTag
//                      )
//                  } else  {
//                      buildCount++
//                      builder10.append(newTag)
//                  }
//
//                }
//            }
//
//                    Log.d("CHECKTAGS", builder.toString())
//        Log.d("CHECKTAGS", builder2.toString())
//        Log.d("CHECKTAGS", builder3.toString())
//        Log.d("CHECKTAGS", builder4.toString())
//        Log.d("CHECKTAGS", builder5.toString())
//
//            Log.d("CHECKTAGS", builder6.toString())
//            Log.d("CHECKTAGS", builder7.toString())
//            Log.d("CHECKTAGS", builder8.toString())
//            Log.d("CHECKTAGS", builder9.toString())
//            Log.d("CHECKTAGS", builder10.toString())
//
//
//        }



    init {


//        calculateTags()

    }



//
//private fun getAllTags () = viewModelScope.launch {
//
//    val response = radioSource.getAllTags()
//
//
//    var included = 0
//    var excluded = 0
//
//
//
//    response.body()?.let{
//
//        it.forEach{
//            included += it.stationcount
//        }
//
//
//        Log.d("CHECKTAGS", "initial size : ${it.size}")
//
//        val withoutMinors =  it.filter {
//            it.stationcount > 7
//        }
//
//        Log.d("CHECKTAGS", "minus minors : ${withoutMinors.size}")
//
//
//        val withoutObvious = mutableListOf<RadioTagsItem>()
//
//        withoutMinors.forEach { item ->
//
//            var isFind = true
//
//            for(i in listOfTags.indices){
//
//                if(item.name.contains(listOfTags[i], ignoreCase = false)){
//                    isFind = false
//                    break
//                }
//
//            }
//
//            if(isFind){
//                withoutObvious.add(item)
//            }
//        }
//
//        Log.d("CHECKTAGS", "minus included : ${withoutObvious.size}")
//
//
//        withoutObvious.forEach {
//
//            excluded += it.stationcount
//
//            Log.d("CHECKTAGS", "name : ${it.name}, num : ${it.stationcount})")
//
//
//        }
//
//
//        Log.d("CHECKTAGS", "included stations : $included, excluded : $excluded")
//
//    }
//
//}
//
//    init {
//
//        getAllTags()
//    }
//


}

