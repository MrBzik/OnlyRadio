package com.example.radioplayer.exoPlayer


import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import com.example.radioplayer.data.local.RadioDAO
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.DateWithStations
import com.example.radioplayer.data.remote.RadioApi
import com.example.radioplayer.data.remote.entities.*
import com.example.radioplayer.exoPlayer.State.*

import javax.inject.Inject

class RadioSource @Inject constructor(
    private val radioApi: RadioApi,
    private val radioDAO: RadioDAO

) {
    var stations = mutableListOf<MediaMetadataCompat>()
    private var _stations : RadioStations? = RadioStations()

    val subscribeToFavouredStations = radioDAO.getAllFavouredStations()
    var stationsFavoured = mutableListOf<MediaMetadataCompat>()

    var stationsFromPlaylist = mutableListOf<MediaMetadataCompat>()

    var stationsFromHistory = mutableListOf<MediaMetadataCompat>()


    suspend fun getStationsInDate(limit: Int, offset: Int, initialDate : String) : DateWithStations{
        val response = radioDAO.getStationsInDate(limit, offset)
        val date = response.date.date
        if(date == initialDate){
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


    suspend fun getStationsInPlaylist(playlistName : String) : List<RadioStation> {

        val response = radioDAO.getStationsInPlaylist(playlistName).first().radioStations

        stationsFromPlaylist = response.map { station ->
            stationToMediaMetadataCompat(station)
        }.toMutableList()

        return response
    }


    suspend fun getRadioStationsSource (isTopSearch : Boolean,
           country : String = "", tag : String = "", name : String = "", offset : Int = 0, pageSize : Int
    ) : RadioStations? {

        val response = if(isTopSearch){
            radioApi.getTopVotedStations(offset = offset, limit = pageSize)
        } else {
            if(country != "") {
                radioApi.searchRadio(country, tag, name, offset = offset, limit = pageSize)
            } else {
                radioApi.searchRadioWithoutCountry(tag = tag, name = name, offset = offset, limit = pageSize)
            }

        }
        _stations = response.body()

        return response.body()
    }



    fun createMediaItemsFromDB(listOfStations : List<RadioStation>){

        stationsFavoured = listOfStations.map { station ->
            stationToMediaMetadataCompat(station)
        }.toMutableList()

    }

    suspend fun getRadioStations (isNewSearch : Boolean)   {

            state = STATE_PROCESSING

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

                        state = STATE_INITIALIZED
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




    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(value == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action : (Boolean) -> Unit) : Boolean {

        if(state == STATE_CREATED || state == STATE_PROCESSING) {
            onReadyListeners += action
            return false
        }
        else {
            action(state == STATE_INITIALIZED)
            return true
        }
    }


}

enum class State {

    STATE_CREATED,
    STATE_PROCESSING,
    STATE_INITIALIZED,
    STATE_ERROR

}