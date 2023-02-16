package com.example.radioplayer.exoPlayer.callbacks

import android.app.Service.STOP_FOREGROUND_DETACH
import android.app.Service.STOP_FOREGROUND_LEGACY
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.radioplayer.exoPlayer.RadioService
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class RadioPlayerEventListener (
    private val radioService : RadioService
        ) : Player.Listener {


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        super.onPlayWhenReadyChanged(playWhenReady, playbackState)

        if(playbackState == Player.STATE_READY && !playWhenReady) {

            if(Build.VERSION.SDK_INT > 24) { radioService.stopForeground(STOP_FOREGROUND_DETACH)}
            else {radioService.stopForeground(false)}
            radioService.isForegroundService = false
        }

    }


    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(radioService, "Radio station does not respond", Toast.LENGTH_SHORT).show()
    }
}