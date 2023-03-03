package com.example.radioplayer.exoPlayer

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.media.MediaBrowserServiceCompat
import com.bumptech.glide.RequestManager
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.exoPlayer.callbacks.RadioPlaybackPreparer
import com.example.radioplayer.exoPlayer.callbacks.RadioPlayerEventListener
import com.example.radioplayer.exoPlayer.callbacks.RadioPlayerNotificationListener
import com.example.radioplayer.utils.Constants.MEDIA_ROOT_ID
import com.example.radioplayer.utils.Constants.NETWORK_ERROR
import com.example.radioplayer.utils.Constants.COMMAND_NEW_SEARCH
import com.example.radioplayer.utils.Constants.COMMAND_START_RECORDING
import com.example.radioplayer.utils.Constants.COMMAND_STOP_RECORDING
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.example.radioplayer.utils.Utils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource.Factory
import dagger.hilt.android.AndroidEntryPoint
import dev.brookmg.exorecord.lib.ExoRecord
import dev.brookmg.exorecord.lib.IExoRecord
import dev.brookmg.exorecordogg.ExoRecordOgg
import kotlinx.coroutines.*
import java.util.*
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

    @Inject
    lateinit var exoRecord: ExoRecord

    @Inject
    lateinit var radioServiceConnection: RadioServiceConnection

    private val serviceJob = SupervisorJob()

    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var radioNotificationManager: RadioNotificationManager

    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var mediaSessionConnector : MediaSessionConnector

    private lateinit var radioPlayerEventListener: RadioPlayerEventListener

    var isForegroundService = false

    private var currentStation : MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    private var isFromRecording = false

    private val observerForDatabase = Observer<List<RadioStation>>{
        radioSource.createMediaItemsFromDB(it)
    }

    private val observerForRecordings = Observer<List<Recording>>{
        radioSource.createMediaItemsFromRecordings(it)
    }

    companion object{
        var curRecordTotalDuration = MutableLiveData(0L)
        val recordingPlaybackPosition = MutableLiveData<Long>()
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
            ) {
//            if(isFromRecording){
//                curRecordTotalDuration = exoPlayer.duration
//            }
        }

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
                SEARCH_FROM_RECORDINGS -> {
                    preparePlayer(
                        radioSource.recordings,
                        itemToPlay,
                        playNow = true,
                        isFromRecordings = true
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

                COMMAND_START_RECORDING -> {

                    if(isExoRecordListenerToSet){
                        setExoRecordListener()
                    }
                    startRecording()
                }

                COMMAND_STOP_RECORDING -> {
                    stopRecording()
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
        radioSource.allRecordingsLiveData.observeForever(observerForRecordings)

//        recordingPlaybackPosition.observeForever(){
//
//            Log.d("CHECKTAGS", "duration : $curRecordTotalDuration, position : $it")
//
//        }



    }


    private var isRecordingDurationListenerRunning = false
    var isPlaybackStatePlaying = false


    fun listenToRecordDuration ()  {

        if(!isRecordingDurationListenerRunning && isFromRecording){
            isRecordingDurationListenerRunning = true

            val duration = exoPlayer.duration
                curRecordTotalDuration.postValue(duration)

                  CoroutineScope(Dispatchers.IO).launch {

                while (isFromRecording && isPlaybackStatePlaying){

                    val pos = radioServiceConnection.playbackState.value?.currentPlaybackPosition ?: 0L

                    if(pos >= duration) {

                        withContext(Dispatchers.Main + serviceJob){
                            exoPlayer.seekTo(0)
                            exoPlayer.playWhenReady = false
                            recordingPlaybackPosition.postValue(0)
                        }
                        break
                    }

                    recordingPlaybackPosition.postValue(pos)
                    delay(500)
                }
                isRecordingDurationListenerRunning = false
            }
        }
    }

    private val exoRecordListener = object : ExoRecord.ExoRecordListener{

        lateinit var timer : Timer

        override fun onStartRecording(recordFileName: String) {
            radioSource.newExoRecord = recordFileName.replace(".wav", ".ogg")
            radioSource.exoRecordState.postValue(true)
            radioSource.exoRecordFinishConverting.postValue(false)

            timer = Timer()
            timer.scheduleAtFixedRate(object : TimerTask() {
                val startTime = System.currentTimeMillis()
                override fun run() {
                    val differance = (System.currentTimeMillis() - startTime)
                    radioSource.exoRecordTimer.postValue(Utils.timerFormat(differance))
                }
            }, 0L, 1000L)


        }
        override fun onStopRecording(record: IExoRecord.Record) {

            timer.cancel()
            isConverterWorking = true
            radioSource.exoRecordState.postValue(false)

            CoroutineScope(Dispatchers.IO).launch {
                val converter = ExoRecordOgg.convertFile(
                    this@RadioService.application,
                    record.filePath,
                    44_100,
                    2,
                    0.4f){ progress ->

                    if(progress == 100.0f){
                        Log.d("CHECKTAGS", "over")
                        try {
                            deleteFile(record.filePath)
                            isConverterWorking = false
                            radioSource.exoRecordFinishConverting.postValue(true)
                        } catch (e: java.lang.Exception){
                            Log.d("CHECKTAGS", e.stackTraceToString())
                        }
                        this.cancel()
                    }
                }
            }
        }
    }

    private var isExoRecordListenerToSet = true

    private var isConverterWorking = false

    private fun setExoRecordListener(){
        exoRecord.addExoRecordListener("MainListener", exoRecordListener)
        isExoRecordListenerToSet = false
    }


    private fun startRecording () = serviceScope.launch {
        if(!isConverterWorking){
            exoRecord.startRecording()
        }
    }

    private fun stopRecording () = serviceScope.launch {
       if(!isConverterWorking){
           exoRecord.stopRecording()
       }
    }






   private inner class RadioQueueNavigator : TimelineQueueNavigator(mediaSession){

       override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {

            currentStation?.let {

             return it.description

           } ?: return radioSource.stations.first().description

       }
   }

    private fun preparePlayer(
        stations : List<MediaMetadataCompat>,
        itemToPlay : MediaMetadataCompat?,
        playNow : Boolean,
        isFromRecordings : Boolean = false
    ){

        this.isFromRecording = isFromRecordings

      val uri = try {
          stations[stations.indexOf(itemToPlay)].getString(METADATA_KEY_MEDIA_URI)
      } catch (e : ArrayIndexOutOfBoundsException){
          ""
      }

       val mediaItem = if(isFromRecordings){
           val uriOgg = "${this.filesDir.path}/$uri"
           MediaItem.fromUri(uriOgg.toUri())
       } else {
           MediaItem.fromUri(uri.toUri())
       }

      val mediaSource = if(uri.contains("m3u8")) {
          HlsMediaSource.Factory(dataSourceFactory)
              .createMediaSource(mediaItem)
      } else {
          ProgressiveMediaSource.Factory(dataSourceFactory)
              .createMediaSource(mediaItem)
      }

        exoPlayer.skipSilenceEnabled = true

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
        radioSource.allRecordingsLiveData.removeObserver(observerForRecordings)

        exoRecord.removeExoRecordListener("MainListener")

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