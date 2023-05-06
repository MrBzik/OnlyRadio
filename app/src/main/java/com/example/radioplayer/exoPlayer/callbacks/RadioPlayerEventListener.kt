package com.example.radioplayer.exoPlayer.callbacks

import android.app.Service.STOP_FOREGROUND_DETACH
import android.util.Log
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY_ONE_DATE
import com.example.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.example.radioplayer.utils.Constants.TITLE_UNKNOWN
import com.example.radioplayer.utils.toRadioStation
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.metadata.Metadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RadioPlayerEventListener (
    private val radioService : RadioService
        ) : Player.Listener {

    private var isMetadataUpdating = false
    private var lastTitle = ""

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {

        if(lastTitle != mediaMetadata.title.toString()){

            lastTitle = mediaMetadata.title.toString()

//            if(!isMetadataUpdating){
//                radioService.serviceScope.launch {
//                    isMetadataUpdating = true
//                    delay(1000)
            super.onMediaMetadataChanged(mediaMetadata)
                    val withoutWalm = lastTitle
                        .replace("WALMRadio.com", "")

                    if(!RadioService.isFromRecording){
                        if(withoutWalm.equals("NULL", ignoreCase = true) || withoutWalm.isBlank()
                            || withoutWalm.length < 3 || withoutWalm.isDigitsOnly() ||
                            withoutWalm.equals("unknown", true)
                            || withoutWalm.contains("{\"STATUS\"", true)
                        ){
                            RadioService.currentSongTitle.postValue("")
                            RadioService.currenlyPlaingSong = TITLE_UNKNOWN

                        } else {
                            RadioService.currentSongTitle.postValue(withoutWalm)
                            radioService.insertNewTitle(withoutWalm)
                            RadioService.currenlyPlaingSong = withoutWalm
                        }
                    }
                    radioService.invalidateNotification()
//                    isMetadataUpdating = false
//                }
//            }

        }
    }


    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)

        val uri = radioService.exoPlayer.currentMediaItem?.localConfiguration?.uri
        val index = radioService.exoPlayer.currentMediaItemIndex
        var station : RadioStation? = null
        RadioService.currentPlayingItemPosition = index


        when(RadioService.currentPlaylist){

            SEARCH_FROM_API ->
                station = radioService.radioSource.stationsFromApi[index].toRadioStation()

            SEARCH_FROM_FAVOURITES ->
                station = radioService.radioSource.stationsFavoured[index]

            SEARCH_FROM_PLAYLIST ->
                station = RadioSource.stationsInPlaylist[index]

            SEARCH_FROM_HISTORY ->
                station = radioService.radioSource.stationsFromHistory[index]

            SEARCH_FROM_HISTORY_ONE_DATE ->
                station = radioService.radioSource.stationsFromHistoryOneDate[index]

            SEARCH_FROM_RECORDINGS -> {
                val recording = radioService.stationsFromRecordings[index]
                radioService.currentRecording = recording
                RadioService.currentPlayingRecording.postValue(recording)
//                radioService.serviceScope.launch {
//                    delay(1000)
//                    radioService.invalidateNotification()
//                }
            }


        }

        station?.let { radioService.currentRadioStation = it }



        RadioService.currentPlayingStation.postValue(station)

        Log.d("CHECKTAGS", "uri of playing mediaitem is $uri")
        Log.d("CHECKTAGS", "index is $index")

    }


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        super.onPlayWhenReadyChanged(playWhenReady, playbackState)

        if(playbackState == Player.STATE_READY && !playWhenReady || playbackState == Player.STATE_IDLE && !playWhenReady) {

            radioService.isPlaybackStatePlaying = false

            radioService.stopForeground(STOP_FOREGROUND_DETACH)

            radioService.isForegroundService = false

            radioService.exoPlayer.volume = 0f
        }
         else if(playbackState == Player.STATE_READY && playWhenReady){

            radioService.isPlaybackStatePlaying = true
            radioService.listenToRecordDuration()
            radioService.fadeInPlayer()

         }

//        else if(playbackState == Player.STATE_IDLE){
//            Log.d("CHECKTAGS", "event")
//            radioService.radioNotificationManager.removeNotification()
//        }

    }




    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)

        if(RadioService.isToReconnect && radioService.exoPlayer.playWhenReady){
            radioService.exoPlayer.prepare()

           Toast.makeText(radioService, "Reconnecting...", Toast.LENGTH_SHORT).show()



        } else {
            Toast.makeText(radioService, "Station not responding", Toast.LENGTH_SHORT).show()
        }
    }
}