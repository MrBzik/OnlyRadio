package com.example.radioplayer.exoPlayer


import android.content.SharedPreferences
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.lifecycle.MutableLiveData
import com.example.radioplayer.data.local.RadioDAO
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.data.local.relations.DateWithStations
import com.example.radioplayer.data.remote.RadioApi
import com.example.radioplayer.data.remote.entities.*

import com.example.radioplayer.utils.Constants.API_RADIO_SEARCH_URL
import com.example.radioplayer.utils.Constants.API_RADIO_TOP_VOTE_SEARCH_URL
import com.example.radioplayer.utils.Constants.BASE_RADIO_URL
import com.example.radioplayer.utils.Constants.BASE_RADIO_URL3
import com.example.radioplayer.utils.Constants.listOfUrls

import javax.inject.Inject

class RadioSource @Inject constructor(
    private val radioApi: RadioApi,
    private val radioDAO: RadioDAO,
    private val validBaseUrlPref : SharedPreferences

) {
    var stations = mutableListOf<MediaMetadataCompat>()
    private var _stations: RadioStations? = RadioStations()

    val subscribeToFavouredStations = radioDAO.getAllFavouredStations()
    var stationsFavoured = mutableListOf<MediaMetadataCompat>()

    val allRecordingsLiveData =  radioDAO.getAllRecordings()
    var recordings = mutableListOf<MediaMetadataCompat>()

    var stationsFromPlaylist = mutableListOf<MediaMetadataCompat>()

    var stationsFromHistory = mutableListOf<MediaMetadataCompat>()

    val exoRecordState : MutableLiveData<Boolean> = MutableLiveData(false)
    val exoRecordTimer : MutableLiveData<Long> = MutableLiveData()
    val exoRecordFinishConverting : MutableLiveData<Boolean> = MutableLiveData()



//    suspend fun getAllCountries() = radioApi.getAllCountries()


    suspend fun getAllTags() = radioApi.getAllTags()

    suspend fun insertRecording(recording : Recording) = radioDAO.insertRecording(recording)
    suspend fun deleteRecording(recId : String) = radioDAO.deleteRecording(recId)


    suspend fun getStationsInDate(limit: Int, offset: Int, initialDate: String): DateWithStations {
        val response = radioDAO.getStationsInAllDates(limit, offset)
        val date = response.date.date
        if (date == initialDate) {
            stationsFromHistory = response.radioStations.map { station ->
                stationToMediaMetadataCompat(station)
            }.toMutableList()
        } else {
            response.radioStations.map { station ->
                stationsFromHistory.add(
                    stationToMediaMetadataCompat(station)
                )
            }
        }
        return response
    }

    suspend fun getStationsInOneDate(time : Long) : DateWithStations {
        val response = radioDAO.getStationsInOneDate(time)
        stationsFromHistory = response.radioStations.map{ station ->
            stationToMediaMetadataCompat(station)
        }.toMutableList()
        return response
    }



    fun createMediaItemsFromDB(listOfStations : List<RadioStation>){

        stationsFavoured = listOfStations.map { station ->
            stationToMediaMetadataCompat(station)
        }.toMutableList()

    }

    val isRecordingUpdated : MutableLiveData<Boolean> = MutableLiveData()


    fun handleRecordingsUpdates(
        listOfRecordings : List<Recording>
    ){
//        if(deleteAt != -1){
//            recordings.removeAt(deleteAt)
//        }
//        else if(addRecordingAt != -1){
//            val rec = listOfRecordings[addRecordingAt]
//            val mediaRec = createMediaMetadataCompatFromRecording(rec)
//            recordings.add(addRecordingAt, mediaRec)
//        }

            recordings = listOfRecordings.map { recording ->
                createMediaMetadataCompatFromRecording(recording)
            }.toMutableList()
            isRecordingUpdated.postValue(true)
    }


    private fun createMediaMetadataCompatFromRecording(recording : Recording) : MediaMetadataCompat{

        return MediaMetadataCompat.Builder()
            .putString(METADATA_KEY_TITLE, recording.name)
            .putString(METADATA_KEY_DISPLAY_TITLE, recording.name)
            .putString(METADATA_KEY_MEDIA_ID, recording.id)
            .putString(METADATA_KEY_ALBUM_ART_URI, recording.iconUri)
            .putString(METADATA_KEY_DISPLAY_ICON_URI, recording.iconUri)
            .putString(METADATA_KEY_MEDIA_URI, recording.id)
            .putString(METADATA_KEY_DISPLAY_SUBTITLE, "recording")
            .build()
    }



//    fun createConcatenatingMediaFromRecordings(
//        dataSourceFactory: DefaultDataSource.Factory,
//        fileDirPath : String
//    )  : ConcatenatingMediaSource {
//        val concatenatingMediaSource = ConcatenatingMediaSource()
//        recordings.forEach { recording ->
//
//            val uri = "$fileDirPath/${recording.getString(METADATA_KEY_MEDIA_URI)}"
//            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(MediaItem.fromUri(uri))
//            concatenatingMediaSource.addMediaSource(mediaSource)
//        }
//            return concatenatingMediaSource
//    }



    suspend fun getStationsInPlaylist(playlistName: String) {

        val response = radioDAO.getStationsInPlaylist(playlistName)

       val list = response?.radioStations ?: emptyList()

        stationsFromPlaylist = list.map { station ->
            stationToMediaMetadataCompat(station)
        }.toMutableList()

    }



    private var validBaseUrl = validBaseUrlPref.getString(BASE_RADIO_URL, BASE_RADIO_URL3)

//    private var validBaseUrl = BASE_RADIO_URLTEST

    private var initialBaseUrlIndex = listOfUrls.indexOf(validBaseUrl)

    private var currentUrlIndex = 0

    suspend fun getRadioStationsSource(
        country: String = "", tag: String = "", isTagExact : Boolean,
        name: String = "", isNameExact : Boolean,
        offset: Int = 0, pageSize: Int

    ): RadioStations? {

        val tagExact = if(tag.isBlank()) false else isTagExact
        val nameExact = if(name.isBlank()) false else isNameExact

        val response = try {

            if(tag == "" && name == "" && country == ""){
                radioApi.getTopVotedStations(
                    offset = offset, limit = pageSize,
                    url =  "${validBaseUrl}$API_RADIO_TOP_VOTE_SEARCH_URL"
                )
            } else {
                if(country != "") {
                    radioApi.searchRadio(
                        country = country, tag = tag, tagExact = tagExact,
                        name = name, nameExact = nameExact,
                        offset = offset, limit = pageSize,
                        url = "${validBaseUrl}$API_RADIO_SEARCH_URL"
                    )
                } else {
                    radioApi.searchRadioWithoutCountry(
                        tag = tag, tagExact = tagExact,
                        name = name, nameExact = nameExact,
                        offset = offset, limit = pageSize,
                        url = "${validBaseUrl}$API_RADIO_SEARCH_URL"
                    )
                }
            }
        } catch (e : Exception){
            null
        }
                if(response == null) {

                    for(i in currentUrlIndex until listOfUrls.size){

                        if(i == initialBaseUrlIndex) {
                            currentUrlIndex++
                        }
                        else {
                            currentUrlIndex ++
                            validBaseUrl = listOfUrls[i]
                            return getRadioStationsSource(
                                country, tag, isTagExact, name, isNameExact, offset, pageSize)
                        }
                    }
                }

                if(currentUrlIndex > 0){
                    validBaseUrlPref.edit().putString(BASE_RADIO_URL, listOfUrls[currentUrlIndex-1]).apply()
                    currentUrlIndex = 0
                }

            _stations = response?.body()
        return response?.body()

        }



    suspend fun getRadioStations (isNewSearch : Boolean)   {

//            state = STATE_PROCESSING

              _stations?.let {

                  if(isNewSearch) {
                      stations = it.map { station ->
                          stationItemToMediaMetadataCompat(station)
                      }.toMutableList()
                  } else {

                      it.map { station ->

                          stations.add(
                              stationItemToMediaMetadataCompat(station)
                          )
                      }
                  }

//                     state = STATE_INITIALIZED
                }
    }


    private fun stationItemToMediaMetadataCompat(station : RadioStationsItem)
            = MediaMetadataCompat.Builder()
        .putString(METADATA_KEY_TITLE, station.name)
        .putString(METADATA_KEY_DISPLAY_TITLE, station.name)
        .putString(METADATA_KEY_MEDIA_ID, station.stationuuid)
        .putString(METADATA_KEY_ALBUM_ART_URI, station.favicon)
        .putString(METADATA_KEY_DISPLAY_ICON_URI, station.favicon)
        .putString(METADATA_KEY_MEDIA_URI, station.url_resolved)
        .putString(METADATA_KEY_DISPLAY_SUBTITLE, station.country)
        .build()


    private fun stationToMediaMetadataCompat(station : RadioStation)
        = MediaMetadataCompat.Builder()
        .putString(METADATA_KEY_TITLE, station.name)
        .putString(METADATA_KEY_DISPLAY_TITLE, station.name)
        .putString(METADATA_KEY_MEDIA_ID, station.stationuuid)
        .putString(METADATA_KEY_ALBUM_ART_URI, station.favicon)
        .putString(METADATA_KEY_DISPLAY_ICON_URI, station.favicon)
        .putString(METADATA_KEY_MEDIA_URI, station.url)
        .putString(METADATA_KEY_DISPLAY_SUBTITLE, station.country)
        .build()




//    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

//    private var state = STATE_CREATED
//        set(value) {
//            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
//                synchronized(onReadyListeners) {
//                    field = value
//                    onReadyListeners.forEach { listener ->
//                        listener(value == STATE_INITIALIZED)
//                    }
//                }
//            } else {
//                field = value
//            }
//        }

//    fun whenReady(action : (Boolean) -> Unit) : Boolean {
//
//        if(state == STATE_CREATED || state == STATE_PROCESSING) {
//            onReadyListeners += action
//            return false
//        }
//        else {
//            action(state == STATE_INITIALIZED)
//            return true
//        }
//    }


}

//enum class State {
//
//    STATE_CREATED,
//    STATE_PROCESSING,
//    STATE_INITIALIZED,
//    STATE_ERROR
//
//}