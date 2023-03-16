package com.example.radioplayer.exoPlayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.utils.Constants.PLAY_WHEN_READY
import com.example.radioplayer.utils.Constants.SEARCH_FLAG
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

class RadioPlaybackPreparer (

    private val radioSource: RadioSource,
    private val playerPrepared : (MediaMetadataCompat?, flag: Int, playWhenReady : Boolean) -> Unit,
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


        var flag = 666

        var isToPlay = true

        extras?.let {
            flag = it.getInt(SEARCH_FLAG, 0)
             isToPlay = it.getBoolean(PLAY_WHEN_READY, true)

        }


//        radioSource.whenReady {
            val itemToPlay =
                    when(flag){
                        SEARCH_FROM_API -> radioSource.stations.find {
                            it.description.mediaId == mediaId
                        }
                         SEARCH_FROM_FAVOURITES -> radioSource.stationsFavoured.find {
                             it.description.mediaId == mediaId
                         }
                          SEARCH_FROM_HISTORY -> radioSource.stationsFromHistory.find{
                              it.description.mediaId == mediaId
                          }
                           SEARCH_FROM_RECORDINGS -> {

                               radioSource.recordings.find {
                                   it.description.mediaId == mediaId
                               }
                           }
                         else -> radioSource.stationsFromPlaylist.find {
                             it.description.mediaId == mediaId
                         }
                    }

            playerPrepared(itemToPlay, flag, isToPlay)
//        }

    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {}

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {}


}