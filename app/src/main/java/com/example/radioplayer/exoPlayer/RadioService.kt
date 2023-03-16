package com.example.radioplayer.exoPlayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaExtractor
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.media.MediaBrowserServiceCompat
import com.arthenica.ffmpegkit.FFmpegKit
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.exoPlayer.callbacks.RadioPlaybackPreparer
import com.example.radioplayer.exoPlayer.callbacks.RadioPlayerEventListener
import com.example.radioplayer.exoPlayer.callbacks.RadioPlayerNotificationListener

import com.example.radioplayer.utils.Constants.MEDIA_ROOT_ID
import com.example.radioplayer.utils.Constants.COMMAND_NEW_SEARCH
import com.example.radioplayer.utils.Constants.COMMAND_START_RECORDING
import com.example.radioplayer.utils.Constants.COMMAND_STOP_RECORDING

import com.example.radioplayer.utils.Constants.COMMAND_REMOVE_CURRENT_PLAYING_ITEM
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_PLAYBACK_SPEED
import com.example.radioplayer.utils.Constants.RECORDING_CHANNEL_ID

import com.example.radioplayer.utils.Constants.RECORDING_QUALITY_PREF
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioProcessor
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource.Factory
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.brookmg.exorecord.lib.ExoRecord
import dev.brookmg.exorecord.lib.IExoRecord
import dev.brookmg.exorecordogg.ExoRecordOgg
import kotlinx.coroutines.*
import java.io.File
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

    lateinit var radioNotificationManager: RadioNotificationManager

    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var mediaSessionConnector : MediaSessionConnector

    private lateinit var radioPlayerEventListener: RadioPlayerEventListener

    var isForegroundService = false

    private var currentStation : MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    private var isFromRecording = false

//    private var deleteRecordingAt = -1
//    private var addRecordingAt = -1

    private val observerForDatabase = Observer<List<RadioStation>>{
        radioSource.createMediaItemsFromDB(it)
    }

    private val observerForRecordings = Observer<List<Recording>>{
        Log.d("CHECKTAGS", "updated recordings")
        radioSource.handleRecordingsUpdates(it)
    }

    companion object{
        val currentSongTitle = MutableLiveData<String>()
        val recordingPlaybackPosition = MutableLiveData<Long>()
        val recordingDuration = MutableLiveData<Long>()
        var playbackSpeed = 100
    }


    private val recQualityPref : SharedPreferences by lazy{
        this.application.getSharedPreferences(RECORDING_QUALITY_PREF, Context.MODE_PRIVATE)
    }



    private var recSampleRate = 0
    private var recChannelsCount = 2

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
             exoPlayer.mediaMetadata.title
        }


        val radioPlaybackPreparer = RadioPlaybackPreparer(
            radioSource, { itemToPlay, flag, playWhenRady ->

            currentStation = itemToPlay

            when (flag) {
                SEARCH_FROM_FAVOURITES -> {
                    preparePlayer(
                        radioSource.stationsFavoured,
                        itemToPlay,
                        playWhenRady
                    )
                }
                SEARCH_FROM_API -> {
                    preparePlayer(
                        radioSource.stations,
                        itemToPlay,
                        playWhenRady
                    )
                }
                SEARCH_FROM_HISTORY -> {
                    preparePlayer(
                        radioSource.stationsFromHistory,
                        itemToPlay,
                        playWhenRady
                    )
                }
                SEARCH_FROM_RECORDINGS -> {
                    preparePlayer(
                        radioSource.recordings,
                        itemToPlay,
                        playNow = playWhenRady,
                        isFromRecordings = true
                    )
                }

                else -> {
                    preparePlayer(
                        radioSource.stationsFromPlaylist,
                        itemToPlay,
                        playWhenRady
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
                        createRecordingNotificationChannel()
                    }
                    startRecording()
                }

                COMMAND_STOP_RECORDING -> {
                    stopRecording()
                }

                COMMAND_REMOVE_CURRENT_PLAYING_ITEM ->{
                    exoPlayer.clearMediaItems()
                }

                COMMAND_UPDATE_PLAYBACK_SPEED -> {

                    val isToPlay = isPlaybackStatePlaying
                    exoPlayer.stop()
                    val newValue = playbackSpeed.toFloat()/100
                    val params = PlaybackParameters(newValue, newValue)
                    exoPlayer.playbackParameters = params
                    exoPlayer.prepare()
                    if(isToPlay){
                        exoPlayer.play()
                    }
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



    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {

            if(it.action == COMMAND_STOP_RECORDING) {

                Log.d("CHECKTAGS", "got message")
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


//    class RecordingNotificationReceiver : BroadcastReceiver(){
//
//        override fun onReceive(context: Context?, intent: Intent?) {
//            Log.d("CHECKTAGS", "receiver got message")
//        }
//
//    }

    private fun createRecordingNotificationChannel(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                RECORDING_CHANNEL_ID, "Recording",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Shows ongoing recording"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }


    }



    private var isRecordingDurationListenerRunning = false
    var isPlaybackStatePlaying = false


    fun listenToRecordDuration ()  {

        serviceScope.launch {

            while (true){

                val format = exoPlayer.audioFormat

                val sampleRate = format?.sampleRate
                val channels = format?.channelCount


                val mime = format?.sampleMimeType
                val container = format?.containerMimeType

                Log.d("CHECKTAGS", "sampleRate: $sampleRate, channels: $channels, mime :$mime," +
                        "container: $container")


                delay(7000)

            }

        }



        if(isFromRecording && !isRecordingDurationListenerRunning){

            isRecordingDurationListenerRunning = true

                  serviceScope.launch {

                while (isFromRecording && isPlaybackStatePlaying){

                    val pos = radioServiceConnection.playbackState.value?.currentPlaybackPosition ?: 0L

                    if(exoPlayer.duration in 0..pos) {

                       exoPlayer.seekTo(0)
                       exoPlayer.playWhenReady = false
                       recordingPlaybackPosition.postValue(0)

                        break
                    }

                    recordingPlaybackPosition.postValue(pos)
                    if(exoPlayer.duration > 0){
                        recordingDuration.postValue(exoPlayer.duration)
                    }

                    delay(500)
                }
                isRecordingDurationListenerRunning = false
            }
        }
    }

    private val exoRecordListener = object : ExoRecord.ExoRecordListener{

        lateinit var timer : Timer
        var duration = 0L

        override fun onStartRecording(recordFileName: String) {

            val notification = RecordingNotification(this@RadioService) {
                currentStation?.getString(METADATA_KEY_TITLE) ?: ""
            }

            notification.showNotification()

            radioSource.exoRecordState.postValue(true)
            radioSource.exoRecordFinishConverting.postValue(false)

            timer = Timer()
            timer.scheduleAtFixedRate(object : TimerTask() {
                val startTime = System.currentTimeMillis()
                override fun run() {
                    duration = (System.currentTimeMillis() - startTime)
                    radioSource.exoRecordTimer.postValue(duration)
                }
            }, 0L, 1000L)


        }
        override fun onStopRecording(record: IExoRecord.Record) {

            timer.cancel()
            isConverterWorking = true
            radioSource.exoRecordState.postValue(false)

                val setting = recQualityPref.getFloat(RECORDING_QUALITY_PREF, 0.4f)
                Log.d("CHECKTAGS", setting.toString())

//                val wavFilePath = this@RadioService.filesDir.absolutePath + File.separator + record.filePath
//                val output = this@RadioService.filesDir.absolutePath + File.separator + record.filePath.split(".").first() + ".ogg"


//                val command = arrayOf( "-i", wavFilePath,
//                    "-ac", recChannelsCount.toString(),"-acodec",
//                    "libvorbis",
//                    output)
//
//
//                FFmpegKit.executeWithArgumentsAsync(command
//                ) { session ->
//
//                    if(session.returnCode.isValueSuccess) {
//                     Log.d("CHECKTAGS", "success")
//
//
//                        CoroutineScope(Dispatchers.IO).launch {
//
//                            try {
//
//                                insertNewRecording(
//                                    record.filePath.split(".").first() + ".ogg",
//                                    System.currentTimeMillis(),
//                                    duration
//                                )
////                                deleteFile(record.filePath)
//                                isConverterWorking = false
//                                radioSource.exoRecordFinishConverting.postValue(true)
//                            } catch (e: java.lang.Exception){
//                                Log.d("CHECKTAGS", e.stackTraceToString())
//                            }
//                        }
//
//                    } else if(session.returnCode.isValueError){
//                        Log.d("CHECKTAGS", "error")
//                    }
//                }


            CoroutineScope(Dispatchers.IO).launch {

                val converter = ExoRecordOgg.convertFile(
                    this@RadioService.application,
                    record.filePath,
                    recSampleRate,
                    recChannelsCount,
                    setting
                ){ progress ->

                    if(progress == 100.0f){
                        try {
                            insertNewRecording(
                                record.filePath,
                                System.currentTimeMillis(),
                                duration
                            )
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


    private suspend fun insertNewRecording(
        filePath : String, timeStamp : Long, duration : Long
    ) {

        val id = filePath.replace(".wav", ".ogg")
        val iconUri = currentStation?.getString(METADATA_KEY_DISPLAY_ICON_URI) ?: ""
        val name = "Rec. ${ currentStation?.getString(METADATA_KEY_DISPLAY_TITLE) ?: ""}"

        radioSource.insertRecording(
            Recording(
                id, iconUri, timeStamp, name, duration
            )
        )
    }


    private var isExoRecordListenerToSet = true

    private var isConverterWorking = false

    private fun setExoRecordListener(){
        exoRecord.addExoRecordListener("MainListener", exoRecordListener)
        isExoRecordListenerToSet = false
    }


    private fun startRecording () = serviceScope.launch {
        if(!isConverterWorking){

            val format = exoPlayer.audioFormat
            val sampleRate = format?.sampleRate ?: 0
            val channels = format?.channelCount ?: 0

//            Log.d("CHECKTAGS", "$sampleRate, $channels")

            recSampleRate = if(sampleRate == 22050 && format?.sampleMimeType == "audio/mp4a-latm" ||
                sampleRate == 24000 && format?.sampleMimeType == "audio/mp4a-latm"
                    ) {
                recChannelsCount = 2
                sampleRate*2
            } else {
                recChannelsCount = channels
                sampleRate
            }

            Log.d("CHECKTAGS", "rec in : $recChannelsCount, $recSampleRate")

//            exoRecord.exoRecordProcessor.configure(AudioProcessor.AudioFormat(sampleRate, channels,  C.ENCODING_PCM_16BIT))

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

           return currentStation?.description ?: radioSource.stations.first().description

       }
   }

    private fun preparePlayer(
        playlist : List<MediaMetadataCompat>,
        itemToPlay : MediaMetadataCompat?,
        playNow : Boolean,
        isFromRecordings : Boolean = false
    ){

        this.isFromRecording = isFromRecordings

        var uri = try {
            playlist[playlist.indexOf(itemToPlay)].getString(METADATA_KEY_MEDIA_URI)
        } catch (e : ArrayIndexOutOfBoundsException){
            ""
        }

         if(isFromRecordings){
             val fileDir = this@RadioService.filesDir.path
             uri = "$fileDir/$uri"
         }

        Log.d("CHECKTAGS", "uri is : $uri")

         val lastPath = uri.toUri().lastPathSegment

         val mediaItem = MediaItem.fromUri(uri.toUri())

         val mediaSource = if(lastPath?.contains("m3u8") == true || lastPath?.contains("m3u") == true) {
             HlsMediaSource.Factory(dataSourceFactory)
                 .createMediaSource(mediaItem)
         } else {
             ProgressiveMediaSource.Factory(dataSourceFactory)
                 .createMediaSource(mediaItem)
         }

         if(!isFromRecordings){

             val params = PlaybackParameters(1f, 1f)
             exoPlayer.playbackParameters = params

             playbackSpeed = 100
         }

         exoPlayer.setMediaSource(mediaSource)
         exoPlayer.prepare()
         exoPlayer.playWhenReady = playNow

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
//       when(parentId){
//           MEDIA_ROOT_ID -> {
//               val resultSent = radioSource.whenReady { isInitialized ->
//                   if(isInitialized){
//                        try{

//                            result.sendResult(radioSource.asMediaItems())

//                            if(!isPlayerInitialized && radioSource.stations.isNotEmpty()) {
//                                preparePlayer(radioSource.stations, radioSource.stations[0], false)
//                                isPlayerInitialized = true
//                            }
//                        } catch (e : java.lang.IllegalStateException){
//                         notifyChildrenChanged(MEDIA_ROOT_ID)
//                        }
//                   } else {
//                       mediaSession.sendSessionEvent(NETWORK_ERROR, null)
//                       result.sendResult(null)
//                   }
//               }
//               if(!resultSent) {
//                   result.detach()
//               }
//           }
//       }
    }


}