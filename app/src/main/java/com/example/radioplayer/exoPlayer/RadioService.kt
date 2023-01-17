package com.example.radioplayer.exoPlayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.radioplayer.exoPlayer.callbacks.RadioPlaybackPreparer
import com.example.radioplayer.exoPlayer.callbacks.RadioPlayerEventListener
import com.example.radioplayer.exoPlayer.callbacks.RadioPlayerNotificationListener
import com.example.radioplayer.utils.Constants.MEDIA_ROOT_ID
import com.example.radioplayer.utils.Constants.NETWORK_ERROR
import com.example.radioplayer.utils.Constants.NEW_SEARCH
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource.Factory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


private const val SERVICE_TAG = "service tag"

@AndroidEntryPoint
class RadioService : MediaBrowserServiceCompat() {


    @Inject
    lateinit var radioServiceConnection: RadioServiceConnection

    @Inject
    lateinit var dataSourceFactory : Factory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var radioSource: RadioSource

    private val serviceJob = Job()

    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var radioNotificationManager: RadioNotificationManager

    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var mediaSessionConnector : MediaSessionConnector

    private lateinit var radioPlayerEventListener: RadioPlayerEventListener

    var isForegroundService = false

    private var currentStation : MediaMetadataCompat? = null

    private var isPlayerInitialized = false


    private fun searchRadioStations(isTopSearch : Boolean = false,
        country : String = "", tag : String = "", name : String = "", offset : Int = 0
    )
            = serviceScope.launch {

        radioSource.getRadioStations(isTopSearch, country, tag, name, offset)
    }


    override fun onCreate() {
        super.onCreate()


        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {

            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        radioNotificationManager = RadioNotificationManager(
            this,
            mediaSession.sessionToken,
            RadioPlayerNotificationListener(this)
            ) {
            // song duration
        }

        val radioPlaybackPreparer = RadioPlaybackPreparer(radioSource, {

            currentStation = it

            preparePlayer(radioSource.stations,
                    it,
                true)
        }, {command, extras ->

            when(command){

                NEW_SEARCH -> {

                    val newTag = extras?.getString("TAG") ?: ""

                    val newName = extras?.getString("NAME") ?: ""

                    val newCountry = extras?.getString("COUNTRY") ?: ""

                    val offset = extras?.getInt("OFFSET") ?: 0

                    val searchTop = extras?.getBoolean("SEARCH_TOP") ?: false

                    searchRadioStations(searchTop, newCountry, newTag, newName, offset)
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
    }


   private inner class RadioQueueNavigator : TimelineQueueNavigator(mediaSession){

       override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {

           return radioSource.stations[windowIndex].description

       }
   }

    private fun preparePlayer(
        stations : List<MediaMetadataCompat>,
        itemToPlay : MediaMetadataCompat?,
        playNow : Boolean
    ){
        var curStationIndex = if(currentStation == null) 0
        else stations.indexOf(itemToPlay)

        exoPlayer.setMediaSource(radioSource.asMediaSource(dataSourceFactory))
        exoPlayer.prepare()
        exoPlayer.seekTo(curStationIndex, 0L)
        exoPlayer.playWhenReady = playNow

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        exoPlayer.stop()
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.removeListener(radioPlayerEventListener)
        exoPlayer.release()
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
                            result.sendResult(radioSource.asMediaItems())
                            if(!isPlayerInitialized && radioSource.stations.isNotEmpty()) {
                                preparePlayer(radioSource.stations, radioSource.stations[0], false)
                                isPlayerInitialized = true
                            }
                        } catch (e : Exception){
                         notifyChildrenChanged(MEDIA_ROOT_ID)
                        }
                   } else {
                       mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                       result.sendResult(null)
                   }
               }
               if(!resultSent) {
                   result.detach()
               }
           }
       }
    }


}