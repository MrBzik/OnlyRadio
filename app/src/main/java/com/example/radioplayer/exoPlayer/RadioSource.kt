package com.example.radioplayer.exoPlayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.radioplayer.data.remote.RadioApi
import com.example.radioplayer.data.remote.entities.RadioStations
import com.example.radioplayer.data.remote.entities.RadioStationsItem
import com.example.radioplayer.exoPlayer.State.*
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class RadioSource @Inject constructor(
    private val radioApi: RadioApi

) {

    var stations = emptyList<MediaMetadataCompat>()

    suspend fun getRadioStations (isTopSearch : Boolean,
        country : String = "", tag : String = "", name : String = ""
            ) = withContext(Dispatchers.IO) {

        state = STATE_PROCESSING

        val response  = if(isTopSearch){
            radioApi.getTopVotedStations()
        } else {
            radioApi.searchRadio(country, tag, name)
        }

        if(response.isSuccessful){
            response.body()?.let {

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
                }
               withContext(Dispatchers.Main){
                   state = STATE_INITIALIZED
               }
            }
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