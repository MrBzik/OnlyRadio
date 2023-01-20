package com.example.radioplayer.exoPlayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.remote.RadioApi
import com.example.radioplayer.data.remote.entities.RadioStations
import com.example.radioplayer.data.remote.entities.RadioStationsItem
import com.example.radioplayer.exoPlayer.State.*
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class RadioSource @Inject constructor(
    private val radioApi: RadioApi

) {

    var stations = mutableListOf<MediaMetadataCompat>()

    var stationsService : RadioStations? = RadioStations()

    suspend fun getRadioStationsSource (isTopSearch : Boolean,
           country : String = "", tag : String = "", name : String = "", offset : Int = 0, pageSize : Int
    ) : RadioStations? {

        val response  = if(isTopSearch){
            radioApi.getTopVotedStations(offset = offset, limit = pageSize)
        } else {
            radioApi.searchRadio(country, tag, name, offset = offset, limit = pageSize)
        }
        stationsService = response.body()

//        Log.d("CHECKNUMZ", stationsService?.size.toString())

        return response.body()
    }

    suspend fun getRadioStations (isNewSearch : Boolean)   {

            state = STATE_PROCESSING

              stationsService?.let {

                  if(isNewSearch) {
                      stations = it.map { station ->
                          MediaMetadataCompat.Builder()
                              .putString(METADATA_KEY_TITLE, station.name)
                              .putString(METADATA_KEY_DISPLAY_TITLE, station.name)
                              .putString(METADATA_KEY_MEDIA_ID, station.stationuuid)
                              .putString(METADATA_KEY_ALBUM_ART_URI, station.favicon)
                              .putString(METADATA_KEY_DISPLAY_ICON_URI, station.favicon)
                              .putString(METADATA_KEY_MEDIA_URI, station.url_resolved)
                              .putString(METADATA_KEY_DISPLAY_SUBTITLE, station.country)
                              .putString(METADATA_KEY_DISPLAY_DESCRIPTION, station.country)
                              .build()
                      }.toMutableList()
                  } else {

                      it.map { station ->

                          stations.add(
                              MediaMetadataCompat.Builder()
                                  .putString(METADATA_KEY_TITLE, station.name)
                                  .putString(METADATA_KEY_DISPLAY_TITLE, station.name)
                                  .putString(METADATA_KEY_MEDIA_ID, station.stationuuid)
                                  .putString(METADATA_KEY_ALBUM_ART_URI, station.favicon)
                                  .putString(METADATA_KEY_DISPLAY_ICON_URI, station.favicon)
                                  .putString(METADATA_KEY_MEDIA_URI, station.url_resolved)
                                  .putString(METADATA_KEY_DISPLAY_SUBTITLE, station.country)
                                  .putString(METADATA_KEY_DISPLAY_DESCRIPTION, station.country)
                                  .build()
                          )

                      }

                  }

                        state = STATE_INITIALIZED
                }

    }


    fun asMediaSource(dataSourceFactory : DefaultDataSource.Factory) : ConcatenatingMediaSource {

        val concatenatingMediaSource = ConcatenatingMediaSource()

        stations.forEach { station ->

            val mediaItem = MediaItem.fromUri(station.getString(METADATA_KEY_MEDIA_URI).toUri())
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems () = stations.map { station ->

        val description = MediaDescriptionCompat.Builder()
            .setMediaUri(station.description.mediaUri)
            .setTitle(station.description.title)
            .setSubtitle(station.description.subtitle)
            .setMediaId(station.description.mediaId)
            .setIconUri(station.description.iconUri)
            .build()

        MediaBrowserCompat.MediaItem(description, FLAG_PLAYABLE)

    }.toMutableList()


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