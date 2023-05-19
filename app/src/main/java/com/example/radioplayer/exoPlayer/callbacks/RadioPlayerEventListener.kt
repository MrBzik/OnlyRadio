package com.example.radioplayer.exoPlayer.callbacks

import android.app.Service.STOP_FOREGROUND_DETACH
import android.util.Log
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.utils.Constants.NO_PLAYLIST
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY_ONE_DATE
import com.example.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.example.radioplayer.utils.Constants.TITLE_UNKNOWN
import com.example.radioplayer.utils.toRadioStation
import com.google.android.exoplayer2.*

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


                        if(withoutWalm.equals("NULL", ignoreCase = true) || withoutWalm.isBlank()
                            || withoutWalm.length < 3 || withoutWalm.isDigitsOnly() ||
                            withoutWalm.equals("unknown", true)
                            || withoutWalm.contains("{\"STATUS\"", true) ||
                            RadioService.isFromRecording
                        ){
                            RadioService.currentSongTitle.postValue("")
                            RadioService.currentlyPlayingSong = TITLE_UNKNOWN

                        } else {
                            RadioService.currentSongTitle.postValue(withoutWalm)

                            if(radioService.exoPlayer.playWhenReady){
                                radioService.insertNewTitle(withoutWalm)
                            }

                            RadioService.currentlyPlayingSong = withoutWalm
                        }

                    radioService.invalidateNotification()
//                    isMetadataUpdating = false
//                }
//            }

        }
    }


    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if(playWhenReady){
            if(RadioService.currentlyPlayingSong != radioService.lastInsertedSong &&
               RadioService.currentlyPlayingSong != TITLE_UNKNOWN){
                radioService.insertNewTitle(RadioService.currentlyPlayingSong)
            }
        }

        if(radioService.radioSource.exoRecordState.value == true){
            radioService.stopRecording()
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)

//        val uri = radioService.exoPlayer.currentMediaItem?.localConfiguration?.uri.toString()
        val index = radioService.exoPlayer.currentMediaItemIndex
        var station : RadioStation? = null
        RadioService.currentPlayingItemPosition = index

        try {
            when(RadioService.currentMediaItems){

                NO_PLAYLIST -> {

                    station = radioService.currentRadioStation

                }

                SEARCH_FROM_API -> {

                    station = radioService.radioSource.stationsFromApi[index].toRadioStation()
                    radioService.insertRadioStation(station)

                }

                SEARCH_FROM_FAVOURITES ->
                    station = radioService.radioSource.stationsFavoured[index]



                SEARCH_FROM_PLAYLIST ->
                    station = RadioSource.stationsInPlaylist[index]

                SEARCH_FROM_HISTORY ->
                    station = radioService.radioSource.stationsFromHistory[index]
//                station = radioService.radioSource.stationsFromHistory.first {
//                    it.url == uri
//                }

                SEARCH_FROM_HISTORY_ONE_DATE ->
                    station = RadioSource.stationsFromHistoryOneDate[index]

                SEARCH_FROM_RECORDINGS -> {
                    val recording = radioService.stationsFromRecordings[index]
                    radioService.currentRecording = recording
                    RadioService.currentPlayingRecording.postValue(recording)
                }
            }
        } catch (e : Exception){
            Log.d("CHECKTAGS", e.stackTraceToString())
        }




        station?.let { radioService.currentRadioStation = it

            RadioService.currentPlayingStation.postValue(it)

            if(RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS){

                radioService.checkDateAndUpdateHistory(it.stationuuid)

            }

        }


        if(radioService.radioSource.exoRecordState.value == true){
            radioService.stopRecording()
        }

    }


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        super.onPlayWhenReadyChanged(playWhenReady, playbackState)

        if(playbackState == Player.STATE_READY && !playWhenReady || playbackState == Player.STATE_IDLE && !playWhenReady) {

            radioService.isPlaybackStatePlaying = false

            radioService.stopForeground(STOP_FOREGROUND_DETACH)

            radioService.isForegroundService = false



//            radioService.exoPlayer.volume = 0f
        }
         else if(playbackState == Player.STATE_READY && playWhenReady){

            radioService.isPlaybackStatePlaying = true
            radioService.listenToRecordDuration()
            if(RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS){
                radioService.fadeInPlayer()
            } else {
                radioService.exoPlayer.volume = 1f
            }

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