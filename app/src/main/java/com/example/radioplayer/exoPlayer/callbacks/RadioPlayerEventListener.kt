package com.example.radioplayer.exoPlayer.callbacks

import android.app.Service.STOP_FOREGROUND_DETACH
import android.app.Service.STOP_FOREGROUND_LEGACY
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.example.radioplayer.exoPlayer.RadioService
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.metadata.Metadata
import kotlinx.coroutines.*

class RadioPlayerEventListener (
    private val radioService : RadioService
        ) : Player.Listener {

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        radioService.radioNotificationManager.updateNotification()

        val withoutWalm = mediaMetadata.title.toString().replace("WALMRadio.com", "")


        RadioService.currentSongTitle.postValue(withoutWalm)

        if(!radioService.isFromRecording){
            if(withoutWalm.equals("NULL", ignoreCase = true) || withoutWalm.isBlank()
                || withoutWalm.length < 3 || withoutWalm.isDigitsOnly() ||
                        withoutWalm.equals("unknown", true)
            ){

            } else {
                radioService.insertNewTitle(withoutWalm)
            }
        }

    }


//    override fun onVolumeChanged(volume: Float) {
//        super.onVolumeChanged(volume)
//        Log.d("CHECKTAGS", "volume changed")
//    }


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        super.onPlayWhenReadyChanged(playWhenReady, playbackState)

        if(playbackState == Player.STATE_READY && !playWhenReady) {

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
//
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