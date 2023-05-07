package com.example.radioplayer.exoPlayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.utils.Constants.HISTORY_ITEM_ID
import com.example.radioplayer.utils.Constants.ITEM_INDEX
import com.example.radioplayer.utils.Constants.PLAY_WHEN_READY
import com.example.radioplayer.utils.Constants.SEARCH_FLAG
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY_ONE_DATE
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

class RadioPlaybackPreparer (

    private val radioSource: RadioSource,
    private val playerPrepared : (MediaMetadataCompat?, flag: Int,
                                  playWhenReady : Boolean, itemIndex : Int
    ) -> Unit,
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

        var index = -1

        var historyId = ""

        extras?.let {
            flag = it.getInt(SEARCH_FLAG, 0)
            isToPlay = it.getBoolean(PLAY_WHEN_READY, true)
            index = it.getInt(ITEM_INDEX, -1)
            historyId = it.getString(HISTORY_ITEM_ID, "")
        }

        if(flag == SEARCH_FROM_HISTORY){
            if(historyId.isNotBlank()){
                index = radioSource.stationsFromHistory.indexOfFirst {
                    it.stationuuid == historyId
                }
            }
        }


//        radioSource.whenReady {
            val itemToPlay =
                    when(flag){
                        SEARCH_FROM_API -> radioSource.stationsFromApiMetadata.find {
                            it.description.mediaId == mediaId
                        }
                         SEARCH_FROM_FAVOURITES -> radioSource.stationsFavouredMetadata.find {
                             it.description.mediaId == mediaId
                         }
                          SEARCH_FROM_HISTORY -> radioSource.stationsFromHistoryMetadata.find{
                              it.description.mediaId == mediaId
                          }
                          SEARCH_FROM_HISTORY_ONE_DATE -> radioSource.stationsFromHistoryOneDateMetadata.find{
                              it.description.mediaId == mediaId
                          }

                           SEARCH_FROM_RECORDINGS -> {

                               radioSource.recordings.find {
                                   it.description.mediaId == mediaId
                               }
                           }
                         else -> radioSource.stationsFromPlaylistMetadata.find {
                             it.description.mediaId == mediaId
                         }
                    }

            playerPrepared(itemToPlay, flag, isToPlay, index)
//        }

    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {}

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {}


}