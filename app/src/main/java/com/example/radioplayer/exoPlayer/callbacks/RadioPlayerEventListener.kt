package com.example.radioplayer.exoPlayer.callbacks

import android.app.Service.STOP_FOREGROUND_DETACH
import android.app.Service.STOP_FOREGROUND_LEGACY
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.radioplayer.exoPlayer.RadioService
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.metadata.Metadata

class RadioPlayerEventListener (
    private val radioService : RadioService
        ) : Player.Listener {

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        radioService.radioNotificationManager.updateNotification()
        RadioService.currentSongTitle.postValue(mediaMetadata.title.toString())
    }


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        super.onPlayWhenReadyChanged(playWhenReady, playbackState)

        if(playbackState == Player.STATE_READY && !playWhenReady) {
            radioService.isPlaybackStatePlaying = false

            if(Build.VERSION.SDK_INT > 24) { radioService.stopForeground(STOP_FOREGROUND_DETACH)}
            else {radioService.stopForeground(false)}
            radioService.isForegroundService = false
        }
         else if(playbackState == Player.STATE_READY && playWhenReady){

            radioService.isPlaybackStatePlaying = true
            radioService.listenToRecordDuration()

         }

    }


    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(radioService, "Radio station does not respond", Toast.LENGTH_SHORT).show()
    }
}