package com.example.radioplayer.exoPlayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.PlaybackStateCompat
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.utils.Constants.IS_CHANGE_MEDIA_ITEMS
import com.example.radioplayer.utils.Constants.IS_SAME_STATION
import com.example.radioplayer.utils.Constants.ITEM_INDEX
import com.example.radioplayer.utils.Constants.PLAY_WHEN_READY
import com.example.radioplayer.utils.Constants.SEARCH_FLAG
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

class RadioPlaybackPreparer (

    private val radioSource: RadioSource,
    private val playerPrepared : (
//        MediaMetadataCompat?,
                                  flag: Int, playWhenReady : Boolean,
                                  itemIndex : Int, isToChangeMediaItems : Boolean,
                                  isSameStation : Boolean
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

        var isToChangeMediaItems = false

        var isSameStation = false

//        var historyId = ""

        extras?.let {
            flag = it.getInt(SEARCH_FLAG, 0)
            isToPlay = it.getBoolean(PLAY_WHEN_READY, true)
            index = it.getInt(ITEM_INDEX, -1)
            isToChangeMediaItems = it.getBoolean(IS_CHANGE_MEDIA_ITEMS, false)
            isSameStation = it.getBoolean(IS_SAME_STATION, false)
        }


            playerPrepared(flag, isToPlay, index, isToChangeMediaItems, isSameStation)

    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {}

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {}


}