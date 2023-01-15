package com.example.radioplayer.exoPlayer.callbacks

import android.widget.Toast
import com.example.radioplayer.exoPlayer.RadioService
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class RadioPlayerEventListener (
    private val radioService : RadioService
        ) : Player.Listener {


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        super.onPlayerStateChanged(playWhenReady, playbackState)

        if(playbackState == Player.STATE_READY && !playWhenReady) {

            radioService.stopForeground(false)

        }
    }


    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(radioService, "Error occurred", Toast.LENGTH_SHORT).show()
    }
}