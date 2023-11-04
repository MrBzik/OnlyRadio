package com.onlyradio.radioplayer.exoPlayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.onlyradio.radioplayer.exoPlayer.RadioSource
import com.onlyradio.radioplayer.utils.Constants.IS_CHANGE_MEDIA_ITEMS
import com.onlyradio.radioplayer.utils.Constants.IS_HISTORY_SWAP
import com.onlyradio.radioplayer.utils.Constants.IS_SAME_STATION
import com.onlyradio.radioplayer.utils.Constants.ITEM_INDEX
import com.onlyradio.radioplayer.utils.Constants.PLAY_WHEN_READY
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FLAG

class RadioPlaybackPreparer (
    private val radioSource: RadioSource,
    private val playerPrepared : (
//        MediaMetadataCompat?,
        flag: Int, playWhenReady : Boolean,
        itemIndex : Int, isToChangeMediaItems : Boolean,
        isSameStation : Boolean,
        isHistorySwap : Boolean
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

        if (extras == null) return

        val flag = extras.getInt(SEARCH_FLAG, 0)

        val isToPlay = extras.getBoolean(PLAY_WHEN_READY, true)

        val index = extras.getInt(ITEM_INDEX, -1)

        val isToChangeMediaItems = extras.getBoolean(IS_CHANGE_MEDIA_ITEMS, false)

        val isSameStation = extras.getBoolean(IS_SAME_STATION, false)

        val isHistorySwap = extras.getBoolean(IS_HISTORY_SWAP, false)

        playerPrepared(flag, isToPlay, index, isToChangeMediaItems, isSameStation, isHistorySwap)

    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {}

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {}


}