package com.example.radioplayer.exoPlayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.radioplayer.exoPlayer.RadioSource
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

class RadioPlaybackPreparer (

    private val radioSource: RadioSource,
    private val playerPrepared : (MediaMetadataCompat?, Boolean) -> Unit,
    private val onCommand : (String, Bundle?) -> Unit
        ) : MediaSessionConnector.PlaybackPreparer {

    override fun onCommand(
        player: Player,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ): Boolean {
        onCommand(command, extras)
        return false
    }

    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepare(playWhenReady: Boolean) {}

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {

        var isFromDB = false

        radioSource.whenReady {
            val itemToPlay = radioSource.stations.find {
                it.description.mediaId == mediaId
            } ?: run {
                isFromDB = true
                radioSource.stationsDB.find{
                    it.description.mediaId == mediaId
                }
            }

            playerPrepared(itemToPlay, isFromDB)
        }

    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {}

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {}


}