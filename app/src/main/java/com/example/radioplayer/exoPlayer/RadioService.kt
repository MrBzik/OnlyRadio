package com.example.radioplayer.exoPlayer

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.media.MediaBrowserServiceCompat
import com.bumptech.glide.RequestManager
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.exoPlayer.callbacks.RadioPlaybackPreparer
import com.example.radioplayer.exoPlayer.callbacks.RadioPlayerEventListener
import com.example.radioplayer.exoPlayer.callbacks.RadioPlayerNotificationListener
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.COMMAND_LOAD_FROM_PLAYLIST
import com.example.radioplayer.utils.Constants.MEDIA_ROOT_ID
import com.example.radioplayer.utils.Constants.NETWORK_ERROR
import com.example.radioplayer.utils.Constants.COMMAND_NEW_SEARCH
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource.Factory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject



private const val SERVICE_TAG = "service tag"

@AndroidEntryPoint
class RadioService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory : Factory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var radioSource: RadioSource

    @Inject
    lateinit var glide : RequestManager

    private val serviceJob = Job()

    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var radioNotificationManager: RadioNotificationManager

    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var mediaSessionConnector : MediaSessionConnector

    private lateinit var radioPlayerEventListener: RadioPlayerEventListener

    var isForegroundService = false

    private var currentStation : MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    private val observerForDatabase = Observer<List<RadioStation>>{
        radioSource.createMediaItemsFromDB(it)
    }



    private fun searchRadioStations(
            isNewSearch : Boolean
    )
            = serviceScope.launch {

        radioSource.getRadioStations(isNewSearch)

    }


    override fun onCreate() {
        super.onCreate()


        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {

            PendingIntent.getActivity(this, 0, it, FLAG_IMMUTABLE)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        radioNotificationManager = RadioNotificationManager(
            this,
            mediaSession.sessionToken,
            RadioPlayerNotificationListener(this),
            glide
            )

        val radioPlaybackPreparer = RadioPlaybackPreparer(radioSource, { itemToPlay, flag ->

            currentStation = itemToPlay

            when (flag) {
                SEARCH_FROM_FAVOURITES -> {
                    preparePlayer(
                        radioSource.stationsFavoured,
                        itemToPlay,
                        true
                    )
                }
                SEARCH_FROM_API -> {
                    preparePlayer(
                        radioSource.stations,
                        itemToPlay,
                        true
                    )
                }
                SEARCH_FROM_HISTORY -> {
                    preparePlayer(
                        radioSource.stationsFromHistory,
                        itemToPlay,
                        true
                    )
                }
                else -> {
                    preparePlayer(
                        radioSource.stationsFromPlaylist,
                        itemToPlay,
                        true
                    )

                }
            }


        }, {
            command, extras ->

            when(command){

                COMMAND_NEW_SEARCH -> {

                   val isNewSearch = extras?.getBoolean("IS_NEW_SEARCH") ?: false

                    searchRadioStations(isNewSearch)

                }
            }

        })


        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(radioPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(RadioQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        radioPlayerEventListener = RadioPlayerEventListener(this)

        exoPlayer.addListener(radioPlayerEventListener)

        radioNotificationManager.showNotification(exoPlayer)



        radioSource.subscribeToFavouredStations.observeForever(observerForDatabase)



    }


   private inner class RadioQueueNavigator : TimelineQueueNavigator(mediaSession){

       override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {

            currentStation?.let {

             return it.description

           } ?: return radioSource.stations[windowIndex].description

       }
   }

    private fun preparePlayer(
        stations : List<MediaMetadataCompat>,
        itemToPlay : MediaMetadataCompat?,
        playNow : Boolean
    ){


      val uri = stations[stations.indexOf(itemToPlay)].getString(METADATA_KEY_MEDIA_URI)
      val mediaItem = MediaItem.fromUri(uri.toUri())

      val mediaSource = if(uri.contains("m3u8")) {
          HlsMediaSource.Factory(dataSourceFactory)
              .createMediaSource(mediaItem)
      } else {
          ProgressiveMediaSource.Factory(dataSourceFactory)
              .createMediaSource(mediaItem)
      }

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = playNow



//        exoPlayer.setMediaSource(radioSource.asMediaSource(dataSourceFactory))
//        exoPlayer.seekTo(curStationIndex, 0L)


    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)


        if(!exoPlayer.isPlaying){
            stopForeground(STOP_FOREGROUND_REMOVE)

            isForegroundService = false
            stopSelf()

            radioNotificationManager.removeNotification()
        }
    }


    override fun onDestroy() {



        serviceScope.cancel()
        radioSource.subscribeToFavouredStations.removeObserver(observerForDatabase)

        exoPlayer.removeListener(radioPlayerEventListener)
        exoPlayer.release()

        mediaSession.apply {
            isActive = false
            release()
        }

    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
       when(parentId){
           MEDIA_ROOT_ID -> {
               val resultSent = radioSource.whenReady { isInitialized ->
                   if(isInitialized){
                        try{

//                            result.sendResult(radioSource.asMediaItems())

//                            if(!isPlayerInitialized && radioSource.stations.isNotEmpty()) {
//                                preparePlayer(radioSource.stations, radioSource.stations[0], false)
//                                isPlayerInitialized = true
//                            }
                        } catch (e : java.lang.IllegalStateException){
                         notifyChildrenChanged(MEDIA_ROOT_ID)
                        }
                   } else {
                       mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                       result.sendResult(null)
                   }
               }
//               if(!resultSent) {
//                   result.detach()
//               }
           }
       }
    }


}