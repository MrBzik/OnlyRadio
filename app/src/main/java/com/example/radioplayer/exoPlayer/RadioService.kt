package com.example.radioplayer.exoPlayer

import android.animation.ValueAnimator
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.Virtualizer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.media.MediaBrowserServiceCompat
import com.bumptech.glide.RequestManager
import com.example.radioplayer.data.local.entities.*
import com.example.radioplayer.data.local.relations.StationDateCrossRef
import com.example.radioplayer.exoPlayer.callbacks.RadioPlaybackPreparer
import com.example.radioplayer.exoPlayer.callbacks.RadioPlayerEventListener
import com.example.radioplayer.exoPlayer.callbacks.RadioPlayerNotificationListener
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.ui.fragments.FavStationsFragment
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.BUFFER_FOR_PLAYBACK
import com.example.radioplayer.utils.Constants.BUFFER_PREF
import com.example.radioplayer.utils.Constants.BUFFER_SIZE_IN_MILLS
import com.example.radioplayer.utils.Constants.COMMAND_ADD_MEDIA_ITEM
import com.example.radioplayer.utils.Constants.COMMAND_CHANGE_BASS_LEVEL
import com.example.radioplayer.utils.Constants.COMMAND_CHANGE_MEDIA_ITEMS
import com.example.radioplayer.utils.Constants.COMMAND_CHANGE_REVERB_MODE
import com.example.radioplayer.utils.Constants.COMMAND_CLEAR_MEDIA_ITEMS
import com.example.radioplayer.utils.Constants.COMMAND_COMPARE_DATES_PREF_AND_CLEAN

import com.example.radioplayer.utils.Constants.MEDIA_ROOT_ID
import com.example.radioplayer.utils.Constants.COMMAND_NEW_SEARCH
import com.example.radioplayer.utils.Constants.COMMAND_ON_DROP_STATION_IN_PLAYLIST
import com.example.radioplayer.utils.Constants.COMMAND_START_RECORDING
import com.example.radioplayer.utils.Constants.COMMAND_STOP_RECORDING

import com.example.radioplayer.utils.Constants.COMMAND_REMOVE_CURRENT_PLAYING_ITEM
import com.example.radioplayer.utils.Constants.COMMAND_REMOVE_MEDIA_ITEM
import com.example.radioplayer.utils.Constants.COMMAND_RESTART_PLAYER
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_FAV_PLAYLIST
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_HISTORY_MEDIA_ITEMS
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_HISTORY_ONE_DATE_MEDIA_ITEMS
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_RADIO_PLAYBACK_PITCH
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_RADIO_PLAYBACK_SPEED
import com.example.radioplayer.utils.Constants.COMMAND_UPDATE_REC_PLAYBACK_SPEED
import com.example.radioplayer.utils.Constants.FOREGROUND_PREF
import com.example.radioplayer.utils.Constants.HISTORY_BOOKMARK_PREF_DEFAULT
import com.example.radioplayer.utils.Constants.HISTORY_DATES_PREF_DEFAULT
import com.example.radioplayer.utils.Constants.HISTORY_PREF
import com.example.radioplayer.utils.Constants.HISTORY_PREF_BOOKMARK
import com.example.radioplayer.utils.Constants.HISTORY_PREF_DATES
import com.example.radioplayer.utils.Constants.IS_ADAPTIVE_LOADER_TO_USE
import com.example.radioplayer.utils.Constants.IS_NEW_SEARCH
import com.example.radioplayer.utils.Constants.ITEM_INDEX
import com.example.radioplayer.utils.Constants.NO_PLAYLIST
import com.example.radioplayer.utils.Constants.PLAY_WHEN_READY
import com.example.radioplayer.utils.Constants.RECONNECT_PREF


import com.example.radioplayer.utils.Constants.RECORDING_QUALITY_PREF
import com.example.radioplayer.utils.Constants.REC_QUALITY_DEF
import com.example.radioplayer.utils.Constants.SEARCH_FLAG
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY_ONE_DATE
import com.example.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.example.radioplayer.utils.Constants.TITLE_UNKNOWN
import com.example.radioplayer.utils.Utils
import com.example.radioplayer.utils.setPreset
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.DefaultLoadControl.*
import com.google.android.exoplayer2.audio.*
import com.google.android.exoplayer2.drm.DrmSessionManagerProvider
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource.Factory
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy
import dagger.hilt.android.AndroidEntryPoint
import dev.brookmg.exorecord.lib.ExoRecord
import dev.brookmg.exorecord.lib.IExoRecord
import dev.brookmg.exorecordogg.ExoRecordOgg
import kotlinx.coroutines.*
import java.sql.Date
import java.util.*
import javax.inject.Inject



private const val SERVICE_TAG = "service tag"

private const val RECORDING_HANDLER = "keeps info about last started recording"
private const val IS_RECORDING_HANDLED = "is recording handled"
private const val RECORDING_FILE_NAME = "file and path of the recording"
private const val RECORDING_NAME = "name of recording"
private const val RECORDING_TIMESTAMP = "rec. time stamp"
private const val RECORDING_ICON_URL = "rec. url path"
private const val RECORDING_SAMPLE_RATE = "rec. sample rate"
private const val RECORDING_CHANNELS_COUNT = "rec. channels count"



@AndroidEntryPoint
class RadioService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory : Factory

    @Inject
    lateinit var renderersFactory: DefaultRenderersFactory

    @Inject
    lateinit var audioAttributes: AudioAttributes

    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var radioSource: RadioSource

    @Inject
    lateinit var glide : RequestManager

    @Inject
    lateinit var exoRecord: ExoRecord

    @Inject
    lateinit var databaseRepository: DatabaseRepository


//    @Inject
//    lateinit var radioServiceConnection: RadioServiceConnection

    private val serviceJob = SupervisorJob()

    val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    lateinit var radioNotificationManager: RadioNotificationManager

    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var mediaSessionConnector : MediaSessionConnector

    private lateinit var radioPlayerEventListener: RadioPlayerEventListener

    private val historySettingsPref by lazy{
        this@RadioService.getSharedPreferences(HISTORY_PREF, Context.MODE_PRIVATE)
    }

    var isForegroundService = false


    private var isPlayerInitialized = false

    private var initialDate = ""
    private val calendar = Calendar.getInstance()


    private val recordingCheck : SharedPreferences by lazy {
        this@RadioService.getSharedPreferences(RECORDING_HANDLER, Context.MODE_PRIVATE)
    }



    private var isFavPlaylistPendingUpdate = false

    private val observerForDatabase by lazy {
        Observer<List<RadioStation>>{

            if(currentMediaItems == SEARCH_FROM_FAVOURITES){
                if(isInStationDetails)
                isFavPlaylistPendingUpdate = true
            } else {
                radioSource.createMediaItemsFromDB(it, exoPlayer, currentRadioStation)
            }

        }
    }



    var stationsFromRecordings = listOf<Recording>()
    var stationsFromRecordingsMediaItems = listOf<MediaItem>()
    var isStationsFromRecordingUpdated = true
    private val observerForRecordings = Observer<List<Recording>>{
        stationsFromRecordings = it

        val fileDir = this@RadioService.filesDir.path

        stationsFromRecordingsMediaItems = it.map { rec ->
            MediaItem.fromUri("$fileDir/${rec.id}")
        }

        isStationsFromRecordingUpdated = true

        radioSource.isRecordingUpdated.postValue(true)
    }

     var currentRadioStation: RadioStation? = null
     var currentRecording : Recording? = null

    var lastInsertedSong = ""

    companion object{

        var isCleanUpNeeded = false
        var historyDatesPref = 3
        var historyPrefBookmark = 20

        var currentPlaylistName = ""
        var selectedHistoryDate = -1L

        var lastDeletedStation : RadioStation? = null


        var currentlyPlaingSong = TITLE_UNKNOWN
//        var currentStation : MediaMetadataCompat? = null
        var currentMediaItems = -1

        var isInStationDetails = false

        var currentPlayingStation : MutableLiveData<RadioStation> = MutableLiveData()
        var currentPlayingRecording : MutableLiveData<Recording> = MutableLiveData()
        var currentPlayingItemPosition = -1

        var isFromRecording = false

        var canOnDestroyBeCalled = false

        var isToKillServiceOnAppClose = false

        var currentDateLong : Long = 0

        val currentSongTitle = MutableLiveData<String>()
        val recordingPlaybackPosition = MutableLiveData<Long>()
        val recordingDuration = MutableLiveData<Long>()

        var playbackSpeedRec = 100
        var playbackSpeedRadio = 100
        var playbackPitchRadio = 100
        var isSpeedPitchLinked = true

        var isToReconnect = true

        var bufferSizeInMills = 0
//        var bufferSizeInBytes = 0
        var bufferForPlayback = 0
//        var isToSetBufferInBytes = false
        var isAdaptiveLoaderToUse = false

        var reverbMode = 0

        var virtualizerLevel = 0


    }


    private var recSampleRate = 0
    private var recChannelsCount = 2

    private fun searchRadioStations(isNewSearch : Boolean) {
        if(isNewSearch && currentMediaItems == SEARCH_FROM_API){
            clearMediaItems()
        }

        radioSource.getRadioStations(isNewSearch, exoPlayer)
    }



    private val environmentalReverb : EnvironmentalReverb by lazy {
        EnvironmentalReverb(1, 0)
    }

    private val effectVirtualizer : Virtualizer by lazy{
        Virtualizer(1, exoPlayer.audioSessionId)
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
            glide, {
                upsertNewBookmark()
            }, {
                stopRecording()
            }
            )


        val radioPlaybackPreparer = RadioPlaybackPreparer(
            radioSource, { flag, playWhenReady, itemIndex, isToChangeMediaItems ->

             var index = itemIndex

            val isFromRecordings = flag == SEARCH_FROM_RECORDINGS

            if(flag == SEARCH_FROM_HISTORY && !isInStationDetails){

                index = adjustIndexFromHistory(itemIndex)
            }

            preparePlayer(
                playNow = playWhenReady,
                itemIndex =  index,
                isToChangeMediaItems = isToChangeMediaItems,
                isFromRecordings = isFromRecordings
            )

        }, {
            command, extras ->

            when(command){

                COMMAND_NEW_SEARCH -> {

                   val isNewSearch = extras?.getBoolean(IS_NEW_SEARCH) ?: false

                    searchRadioStations(isNewSearch)

                }

                COMMAND_START_RECORDING -> {

                    if(isPlaybackStatePlaying){
                        if(isExoRecordListenerToSet){
                            setExoRecordListener()
//                            createRecordingNotificationChannel()
                        }
                        startRecording()
                    }

                }

                COMMAND_STOP_RECORDING -> {
                    stopRecording()
                }

                COMMAND_REMOVE_CURRENT_PLAYING_ITEM ->{
                    exoPlayer.clearMediaItems()
                }

                COMMAND_UPDATE_REC_PLAYBACK_SPEED -> {

                    val isToPlay = isPlaybackStatePlaying
                    exoPlayer.pause()
                    val newValue = playbackSpeedRec.toFloat()/100
                    val params = PlaybackParameters(newValue, newValue)
                    exoPlayer.playbackParameters = params
//                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = isToPlay
                }

                COMMAND_UPDATE_RADIO_PLAYBACK_SPEED -> {

                    if(!isFromRecording){
                        val isToPlay = isPlaybackStatePlaying
                        exoPlayer.pause()
                        if(isSpeedPitchLinked){
                            playbackPitchRadio = playbackSpeedRadio
                        }
                        val params = PlaybackParameters(
                            playbackSpeedRadio.toFloat()/100,
                            playbackPitchRadio.toFloat()/100
                        )
                        exoPlayer.playbackParameters = params
//                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = isToPlay
                    }
                }

                COMMAND_UPDATE_RADIO_PLAYBACK_PITCH -> {

                    if(!isFromRecording){
                        val isToPlay = isPlaybackStatePlaying
                        exoPlayer.pause()
                        if(isSpeedPitchLinked){
                            playbackSpeedRadio = playbackPitchRadio
                        }
                        val params = PlaybackParameters(
                            playbackSpeedRadio.toFloat()/100,
                            playbackPitchRadio.toFloat()/100
                        )
                        exoPlayer.playbackParameters = params
//                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = isToPlay
                    }
                }

                COMMAND_RESTART_PLAYER -> {
                    recreateExoPlayer()
                }


                COMMAND_CHANGE_REVERB_MODE -> {
                    changeReverbMode()
                }

                COMMAND_CHANGE_BASS_LEVEL -> {
                    changeVirtualizerLevel()
                }

                COMMAND_COMPARE_DATES_PREF_AND_CLEAN -> {
                    compareDatesWithPrefAndCLeanIfNeeded(null)
                }

                COMMAND_UPDATE_FAV_PLAYLIST -> {

                    if(isFavPlaylistPendingUpdate){
                        isFavPlaylistPendingUpdate = false
                        radioSource.subscribeToFavouredStations.value?.let {
                            clearMediaItems(false)
                            radioSource.createMediaItemsFromDB(it, exoPlayer, currentRadioStation)

                        }
                    }
                }

                COMMAND_REMOVE_MEDIA_ITEM -> {

                    val index = extras?.getInt(ITEM_INDEX, -1) ?: -1

                    if(index != -1){

                        if(index == exoPlayer.currentMediaItemIndex){

                            clearMediaItems(true)

                        } else {

                            if(index < currentPlayingItemPosition)
                                currentPlayingItemPosition --
                            exoPlayer.removeMediaItem(index)

                            if(currentMediaItems == SEARCH_FROM_FAVOURITES){

                                lastDeletedStation = radioSource.stationsFavoured[index]
                                radioSource.stationsFavoured.removeAt(index)
                                radioSource.stationsFavouredMediaItems.removeAt(index)

                            } else if(currentMediaItems == SEARCH_FROM_PLAYLIST){

                                lastDeletedStation = RadioSource.stationsInPlaylist[index]
                                RadioSource.stationsInPlaylist.removeAt(index)
                                RadioSource.stationsInPlaylistMediaItems.removeAt(index)
                            }
                        }
                    }
                }

                COMMAND_ADD_MEDIA_ITEM -> {

                    val index = extras?.getInt(ITEM_INDEX, -1) ?: -1

                    if(index != -1){

                        lastDeletedStation?.let { station ->
                            val mediaItem = MediaItem.fromUri(station.url!!)
                            exoPlayer.addMediaItem(index, mediaItem)

                            if(index <= currentPlayingItemPosition)
                                currentPlayingItemPosition ++

                            if(currentMediaItems == SEARCH_FROM_FAVOURITES){

                                radioSource.stationsFavoured.add(index, station)
                                radioSource.stationsFavouredMediaItems.add(index, mediaItem)
                            } else if(currentMediaItems == SEARCH_FROM_PLAYLIST){

                                RadioSource.stationsInPlaylist.add(index, station)
                                RadioSource.stationsInPlaylistMediaItems.add(index, mediaItem)

                            }
                        }
                    }

                }

                COMMAND_ON_DROP_STATION_IN_PLAYLIST -> {



                    val radioStation =  FavStationsFragment.dragAndDropStation

                    radioStation?.let { station ->

                        currentPlayingItemPosition ++
                        val mediaItem = MediaItem.fromUri(station.url!!)
                        exoPlayer.addMediaItem(0, mediaItem)
                        RadioSource.stationsInPlaylist.add(0, station)
                        RadioSource.stationsInPlaylistMediaItems.add(0, mediaItem)
                    }
                }

                COMMAND_CLEAR_MEDIA_ITEMS -> {
                    clearMediaItems()
                }


                COMMAND_UPDATE_HISTORY_MEDIA_ITEMS -> {

                    clearMediaItems(false)

                    for(i in 1 until radioSource.stationsFromHistoryMediaItems.size){

                        exoPlayer.addMediaItem(
                            radioSource.stationsFromHistoryMediaItems[i]
                        )
                    }

                    radioSource.isStationsFromHistoryUpdated = false
                }

                COMMAND_CHANGE_MEDIA_ITEMS -> {


                    val playWhenReady = extras?.getBoolean(PLAY_WHEN_READY) ?: false

                    val flag = extras?.getInt(SEARCH_FLAG) ?: -1

                    var index = extras?.getInt(Constants.ITEM_INDEX, -1) ?: 0


                    updateMediaItems(false, flag)


                    currentMediaItems = flag

                    if(flag == SEARCH_FROM_HISTORY){
                        index = adjustIndexFromHistory(index)
                    }

                    exoPlayer.seekTo(index, 0L)

                    exoPlayer.prepare()

                    exoPlayer.playWhenReady = playWhenReady

                }

                COMMAND_UPDATE_HISTORY_ONE_DATE_MEDIA_ITEMS -> {

                    currentPlayingItemPosition = 0

                    clearMediaItems(false)

                    for(i in 1 until RadioSource.stationsFromHistoryOneDateMediaItems.size){

                        exoPlayer.addMediaItem(
                            RadioSource.stationsFromHistoryOneDateMediaItems[i]
                        )
                    }

                    RadioSource.isStationsFromHistoryOneDateUpdated = false
                }

            }
        })

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(radioPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(RadioQueueNavigator())
//        mediaSessionConnector.setPlayer(exoPlayer)


        radioPlayerEventListener = RadioPlayerEventListener(this)

//        exoPlayer.addListener(radioPlayerEventListener)

//        radioNotificationManager.showNotification(exoPlayer)


        checkRecordingAndRecoverIfNeeded()

        isToReconnect = this@RadioService
            .getSharedPreferences(RECONNECT_PREF, Context.MODE_PRIVATE).getBoolean(
            RECONNECT_PREF, true)

        Log.d("CHECKTAGS", "servie code")

        initialCheckForBuffer()

        exoPlayer = provideExoPlayer()
        mediaSessionConnector.setPlayer(exoPlayer)
        radioNotificationManager.showNotification(exoPlayer)

        isToKillServiceOnAppClose = this@RadioService.getSharedPreferences(
            FOREGROUND_PREF, Context.MODE_PRIVATE).getBoolean(FOREGROUND_PREF, false)

        initialHistoryPref()

        registerNewDateReceiver()


        radioSource.subscribeToFavouredStations.observeForever(observerForDatabase)
        radioSource.allRecordingsLiveData.observeForever(observerForRecordings)

        getLastDateAndCheck()

    }


    fun insertNewTitle(title: String){

        serviceScope.launch(Dispatchers.IO){

           val checkTitle = radioSource.checkTitleTimestamp(title, currentDateLong)

           val isTitleBookmarked = checkTitle?.isBookmarked

         checkTitle?.let {
             radioSource.deleteTitle(it)
         }
                val stationName = currentRadioStation?.name ?: ""

                val stationUri = currentRadioStation?.favicon ?: ""


                radioSource.insertNewTitle(Title(
                    timeStamp = System.currentTimeMillis(),
                    date = currentDateLong,
                    title = title,
                    stationName = stationName,
                    stationIconUri = stationUri,
                    isBookmarked = isTitleBookmarked ?: false
                ))

            lastInsertedSong = title

        }
    }



    private fun upsertNewBookmark() = CoroutineScope(Dispatchers.IO).launch{

        if(currentlyPlaingSong != TITLE_UNKNOWN){

            databaseRepository.deleteBookmarkedTitle(currentlyPlaingSong)

            databaseRepository.insertNewBookmarkedTitle(
                BookmarkedTitle(
                    timeStamp = System.currentTimeMillis(),
                    date = currentDateLong,
                    title = currentlyPlaingSong,
                    stationName = currentRadioStation?.name ?: "",
                    stationIconUri = currentRadioStation?.favicon ?: ""
                )
            )

            val count = databaseRepository.countBookmarkedTitles()

            if(count > historyPrefBookmark && historyPrefBookmark != 100){

                val bookmark = databaseRepository.getLastValidBookmarkedTitle(historyPrefBookmark -1)

                databaseRepository.cleanBookmarkedTitles(bookmark.timeStamp)
            }
        }
    }



     fun fadeInPlayer(){
        val anim = ValueAnimator.ofFloat(0f, 1f)

        anim.addUpdateListener {
            exoPlayer.volume = it.animatedValue as Float
        }

        anim.duration = 800
        anim.start()

    }

    private fun fadeOutPlayer(){
        val anim = ValueAnimator.ofFloat(1f, 0f)

        anim.addUpdateListener {
            exoPlayer.volume = it.animatedValue as Float
        }

        anim.duration = 200
        anim.start()

    }



    private var wasReverbSet = false

    private fun changeReverbMode() {

        setPreset(environmentalReverb, reverbMode)


        if(reverbMode == 0){
            environmentalReverb.enabled = false
        } else {
            if(!environmentalReverb.enabled){
                environmentalReverb.enabled = true
            }
        }

//        if(!wasReverbSet){
            exoPlayer.setAuxEffectInfo(AuxEffectInfo(environmentalReverb.id, 1f))
//            wasReverbSet = true
//        }
    }

    private var wasBassBoostSet = false

    private fun changeVirtualizerLevel() {

        if(virtualizerLevel == 0){
            effectVirtualizer.enabled = false
        } else {

            effectVirtualizer.setStrength(virtualizerLevel.toShort())

            if(!effectVirtualizer.enabled){
                effectVirtualizer.enabled = true
            }
        }

//        if(!wasBassBoostSet){
            exoPlayer.setAuxEffectInfo(AuxEffectInfo(effectVirtualizer.id, 1f))
//            wasBassBoostSet = true
//        }
    }


//    private fun changeReverbMode() {
//
//        if(!wasReverbSet){
//            environmentalReverb = EnvironmentalReverb(0, 0)
//        }
//
//        setPreset(environmentalReverb, reverbMode)
//
//        if(reverbMode == 0){
//            environmentalReverb.enabled = false
//            environmentalReverb.release()
//            wasReverbSet = false
//            exoPlayer.clearAuxEffectInfo()
//        } else {
//            environmentalReverb.enabled = true
//            if(!wasReverbSet){
//                exoPlayer.setAuxEffectInfo(AuxEffectInfo(environmentalReverb.id, 1f))
//                wasReverbSet = true
//            }
//        }
//    }

//    private fun changeReverbMode() {
//
//        effectReverb.preset = reverbMode.toShort()
//
//        effectReverb.enabled = reverbMode != 0
//
//      if(!wasReverbSet){
//          exoPlayer.setAuxEffectInfo(AuxEffectInfo(effectReverb.id, 1f))
//          wasReverbSet = true
//      }
//
//    }


    private fun initialCheckForBuffer(){
        val buffPref = this@RadioService.getSharedPreferences(BUFFER_PREF, Context.MODE_PRIVATE)

        bufferSizeInMills = buffPref.getInt(BUFFER_SIZE_IN_MILLS, DEFAULT_MAX_BUFFER_MS)
//        bufferSizeInBytes = buffPref.getInt(BUFFER_SIZE_IN_BYTES, DEFAULT_TARGET_BUFFER_BYTES)
        bufferForPlayback = buffPref.getInt(BUFFER_FOR_PLAYBACK, DEFAULT_BUFFER_FOR_PLAYBACK_MS)
//        isToSetBufferInBytes = buffPref.getBoolean(IS_TO_SET_BUFFER_IN_BYTES, false)
        isAdaptiveLoaderToUse = buffPref.getBoolean(IS_ADAPTIVE_LOADER_TO_USE, false)

    }


//    lateinit var bandwidthMeter: DefaultBandwidthMeter

//    val handler = android.os.Handler(Looper.getMainLooper())
//    val listener = BandwidthMeter.EventListener { elapsedMs, bytesTransferred, bitrateEstimate ->
//
//        Log.d("CHECKTAGS", "elapse : $elapsedMs, bytes transfered : $bytesTransferred, bitrate : $bitrateEstimate")
//
//    }



    private fun provideExoPlayer () : ExoPlayer {

//        val bites = if (!isToSetBufferInBytes) -1
//        else bufferSizeInBytes * 1024

//        bandwidthMeter = DefaultBandwidthMeter.Builder(this@RadioService).build()
//        bandwidthMeter.addEventListener(handler, listener)


       val checkIntervals = if(isAdaptiveLoaderToUse){
           bufferSizeInMills/1000 * 10
       } else 1024


        return ExoPlayer.Builder(this@RadioService, renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setMediaSourceFactory(MyMediaSourceFactory(this@RadioService, dataSourceFactory, checkIntervals))
            .setLoadControl(DefaultLoadControl.Builder()
                .setBufferDurationsMs(bufferSizeInMills, bufferSizeInMills, bufferForPlayback, 5000)
//                .setTargetBufferBytes(bites)
                .build())
//            .setBandwidthMeter(
//                bandwidthMeter
//            )
            .build().apply {
                addListener(radioPlayerEventListener)
            }
    }



    class MyMediaSourceFactory(context: Context, datasourceFactory: DataSource.Factory, checkIntervals: Int) : MediaSource.Factory {
        private val defaultMediaSourceFactory: DefaultMediaSourceFactory
        private val progressiveFactory : MediaSource.Factory

        init {
            defaultMediaSourceFactory = DefaultMediaSourceFactory(context)
            progressiveFactory = ProgressiveMediaSource.Factory(datasourceFactory)
                .setContinueLoadingCheckIntervalBytes(1024 * checkIntervals)
        }


        override fun setDrmSessionManagerProvider(drmSessionManagerProvider: DrmSessionManagerProvider): MediaSource.Factory {
            defaultMediaSourceFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
            progressiveFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
            return this;
        }

        override fun setLoadErrorHandlingPolicy(loadErrorHandlingPolicy: LoadErrorHandlingPolicy): MediaSource.Factory {
            defaultMediaSourceFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
            progressiveFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
            return this
        }

        override fun getSupportedTypes(): IntArray {
            return defaultMediaSourceFactory.supportedTypes
        }

        override fun createMediaSource(mediaItem: MediaItem): MediaSource {
            if (mediaItem.localConfiguration?.uri?.path?.contains("m3u8") == true) {
                return defaultMediaSourceFactory.createMediaSource(mediaItem);

            }
            return progressiveFactory.createMediaSource(mediaItem)
        }
    }



    private fun recreateExoPlayer(){

        val isToPlay = exoPlayer.playWhenReady

        exoPlayer.stop()
        exoPlayer = provideExoPlayer()
        mediaSessionConnector.setPlayer(exoPlayer)

        preparePlayer(
            playNow = isToPlay,
            itemIndex = currentPlayingItemPosition,
            isToChangeMediaItems = true,
            isFromRecordings = currentMediaItems == SEARCH_FROM_RECORDINGS
            )
    }




    private fun checkRecordingAndRecoverIfNeeded(){

        val check = recordingCheck.getBoolean(IS_RECORDING_HANDLED, true)

        if(!check){



            convertRecording(
                recordingCheck.getString(RECORDING_FILE_NAME, "") ?: "",
                recordingCheck.getInt(RECORDING_SAMPLE_RATE, 44100),
                recordingCheck.getInt(RECORDING_CHANNELS_COUNT, 2),
                recordingCheck.getLong(RECORDING_TIMESTAMP, 0),
                300000
            )
        }
    }



//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//
//        intent?.let {
//
//            if(it.action == COMMAND_STOP_RECORDING) {
//
//                stopRecording()

//                NotificationManagerCompat.from(this@RadioService).cancel(RECORDING_NOTIFICATION_ID)
//            }
//        }
//
//        return super.onStartCommand(intent, flags, startId)
//    }



//    private fun createRecordingNotificationChannel(){
//
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//            val channel = NotificationChannel(
//                RECORDING_CHANNEL_ID, "Recording",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            channel.description = "Shows ongoing recording"
//
//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }



    private var isRecordingDurationListenerRunning = false
    var isPlaybackStatePlaying = false


    fun listenToRecordDuration ()  {


        if(reverbMode != 0)
            exoPlayer.setAuxEffectInfo(AuxEffectInfo(environmentalReverb.id, 1f))

        if(virtualizerLevel != 0)
            exoPlayer.setAuxEffectInfo(AuxEffectInfo(effectVirtualizer.id, 1f))


        serviceScope.launch {


            while (true){

                val format = exoPlayer.audioFormat
//
//                val sampleRate = format?.sampleRate



//                Log.d("CHECKTAGS", "sampleRate: $sampleRate, channels: $channels")

//
//                Log.d("CHECKTAGS", "buffer? : ${exoPlayer.totalBufferedDuration}")


                delay(2000)

            }

        }



        // FOR SKIPPING EXCLUSIVE AD

//        serviceScope.launch {
//
//            exoPlayer.playbackParameters = PlaybackParameters(6f)
//            exoPlayer.volume = 0f
//
//            while (true) {
//
//               delay(200)
//
//                if(exoPlayer.currentPosition > 5000)
//                    break
//
//            }
//
//            exoPlayer.playbackParameters = PlaybackParameters(1f)
//            exoPlayer.volume = 1f
//
//        }



        if(isFromRecording && !isRecordingDurationListenerRunning){

            isRecordingDurationListenerRunning = true

                  serviceScope.launch {

                while (isFromRecording && isPlaybackStatePlaying){

                    val pos = mediaSession.controller.playbackState.currentPlaybackPosition

                    if(exoPlayer.duration in 0..pos) {

                       exoPlayer.seekTo(currentPlayingItemPosition, 0)
                        exoPlayer.pause()
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
//                      isToSkip = true
            }
        }
    }



    private val exoRecordListener = object : ExoRecord.ExoRecordListener{

        lateinit var timer : Timer
        var duration = 0L


        override fun onStartRecording(recordFileName: String) {

//            val notification = RecordingNotification(this@RadioService) {
//                currentRadioStation?.name ?: ""
//            }

//            notification.showNotification()

            radioSource.exoRecordState.postValue(true)
            radioSource.exoRecordFinishConverting.postValue(false)

            timer = Timer()
            timer.scheduleAtFixedRate(object : TimerTask() {
                val startTime = System.currentTimeMillis()
                override fun run() {
                    duration = (System.currentTimeMillis() - startTime)

                    val time = Utils.timerFormat(duration)

                    radioNotificationManager.recordingDuration = time
                    radioNotificationManager.updateNotification()

                    radioSource.exoRecordTimer.postValue(time)
                }
            }, 0L, 1000L)

           recordingCheck.edit().apply{
               putBoolean(IS_RECORDING_HANDLED, false)
               putString(RECORDING_FILE_NAME, recordFileName)
               putString(RECORDING_NAME, currentRadioStation?.name ?: "")
               putString(RECORDING_ICON_URL, currentRadioStation?.favicon ?: "")
               putInt(RECORDING_SAMPLE_RATE, recSampleRate)
               putInt(RECORDING_CHANNELS_COUNT, recChannelsCount)
               putLong(RECORDING_TIMESTAMP, System.currentTimeMillis())
           }.apply()


            radioNotificationManager.updateForStartRecording()


        }

        override fun onStopRecording(record: IExoRecord.Record) {

//            NotificationManagerCompat.from(this@RadioService).cancel(RECORDING_NOTIFICATION_ID)


            radioNotificationManager.updateForStopRecording()

            timer.cancel()
            isConverterWorking = true
            radioSource.exoRecordState.postValue(false)

            convertRecording(record.filePath, recSampleRate, recChannelsCount, System.currentTimeMillis(), duration)

        }
    }



    private fun convertRecording(
        filePath: String, sampleRate : Int, channelsCount : Int,
        timeStamp : Long, duration : Long
        )
            = CoroutineScope(Dispatchers.IO).launch {

        val recQualityPref = this@RadioService.application.getSharedPreferences(RECORDING_QUALITY_PREF, Context.MODE_PRIVATE)
        val setting = recQualityPref.getFloat(RECORDING_QUALITY_PREF, REC_QUALITY_DEF)

        Log.d("CHECKTAGS", "setting is : $setting")

        ExoRecordOgg.convertFile(
            this@RadioService.application,
            filePath,
            sampleRate,
            channelsCount,
            setting
        ){ progress ->

            if(progress == 100.0f){
                try {
                    insertNewRecording(
                        filePath,
                        timeStamp,
                        duration
                    )
                    deleteFile(filePath)
                    isConverterWorking = false
                    radioSource.exoRecordFinishConverting.postValue(true)
                    recordingCheck.edit().putBoolean(IS_RECORDING_HANDLED, true).apply()
                } catch (e: java.lang.Exception){
                    Log.d("CHECKTAGS", e.stackTraceToString())
                }
                this.cancel()
            }
        }
    }


    private suspend fun insertNewRecording(
        filePath : String, timeStamp : Long, duration : Long
    ) {

        val id = filePath.replace(".wav", ".ogg")
        val iconUri = recordingCheck.getString(RECORDING_ICON_URL, "") ?: ""
        val name = "Rec. ${ recordingCheck.getString(RECORDING_NAME, "") ?: ""}"

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

            exoRecord.exoRecordProcessor.configure(AudioProcessor.AudioFormat(sampleRate, channels,  C.ENCODING_PCM_16BIT))

            exoRecord.startRecording()
        }
    }

    fun stopRecording () = serviceScope.launch {
       if(!isConverterWorking){
           exoRecord.stopRecording()
       }
    }

    fun invalidateNotification(){
        mediaSessionConnector.invalidateMediaSessionQueue()
        mediaSessionConnector.invalidateMediaSessionMetadata()
        radioNotificationManager.resetBookmarkIcon()
        radioNotificationManager.updateNotification()
    }




   private inner class RadioQueueNavigator : TimelineQueueNavigator(mediaSession){

       override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {

           if(currentMediaItems != SEARCH_FROM_RECORDINGS){
               if(windowIndex != currentPlayingItemPosition){
                   return MediaDescriptionCompat.Builder().build()
               } else {
                   val extra = Bundle()
                   extra.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentlyPlaingSong)
                   return MediaDescriptionCompat.Builder()
                       .setExtras(extra)
                       .setIconUri(currentRadioStation?.favicon?.toUri())
                       .setTitle(currentRadioStation?.name)
                       .setSubtitle(currentlyPlaingSong)
                       .build()
               }
           } else{
               return MediaDescriptionCompat.Builder()
                   .setIconUri(stationsFromRecordings[windowIndex].iconUri.toUri())
                   .setTitle(stationsFromRecordings[windowIndex].name)
                   .build()
           }


//           return currentStation?.description ?: radioSource.stationsFromApiMetadata.first().description

       }
   }



    private fun clearMediaItems(isNoList : Boolean = true){


        if(exoPlayer.currentMediaItemIndex != 0){

            exoPlayer.removeMediaItems(0, exoPlayer.currentMediaItemIndex)
        }

        if(exoPlayer.mediaItemCount != 1){
            exoPlayer.removeMediaItems(1, exoPlayer.mediaItemCount)
        }

        if(isNoList){
            currentPlayingItemPosition = 0
            currentMediaItems = NO_PLAYLIST
        }

    }


    private fun preparePlayer(
//        playlist : List<MediaMetadataCompat>,
//        itemToPlay : MediaMetadataCompat?,
        playNow : Boolean,
        itemIndex : Int = -1,
        isToChangeMediaItems : Boolean = true,
        isFromRecordings : Boolean = false,

    ){

        isFromRecording = isFromRecordings

        val playbackSpeed = (if(isFromRecording) playbackSpeedRec
        else playbackSpeedRadio).toFloat()/100

        val playbackPitch = if(isFromRecording) playbackSpeed
        else playbackPitchRadio.toFloat()/100

        val params = PlaybackParameters(playbackSpeed, playbackPitch)
        exoPlayer.playbackParameters = params


        fadeOutPlayer()

        serviceScope.launch {
            delay(200)


            Log.d("CHECKTAGS", "2, prepare player?. isSamePlaylist = $isToChangeMediaItems")

           updateMediaItems(isToChangeMediaItems, currentMediaItems)

            if(itemIndex != -1){
                exoPlayer.seekTo(itemIndex, 0L)
            }

            exoPlayer.prepare()

            exoPlayer.playWhenReady = playNow
        }




//        playFromUri(uri, playNow, itemIndex, isSamePlaylist)

      }


    private fun updateMediaItems(isToChangeMediaItems : Boolean, flag : Int){

        when(flag){

            SEARCH_FROM_API -> {
                if(isToChangeMediaItems || radioSource.isStationsFromApiUpdated){
                    radioSource.isStationsFromApiUpdated = false
                    exoPlayer.setMediaItems(radioSource.stationsFromApiMediaItems)
                    Log.d("CHECKTAGS", "3 setting media items??")
                }
            }

            SEARCH_FROM_FAVOURITES -> {
                if(isToChangeMediaItems|| radioSource.isStationsFavouredUpdated){
                    radioSource.isStationsFavouredUpdated = false
                    exoPlayer.setMediaItems(radioSource.stationsFavouredMediaItems)
                }
            }

            SEARCH_FROM_PLAYLIST -> {
                if(isToChangeMediaItems || RadioSource.isStationsInPlaylistUpdated){
                    RadioSource.isStationsInPlaylistUpdated = false
                    exoPlayer.setMediaItems(RadioSource.stationsInPlaylistMediaItems)
                }
            }

            SEARCH_FROM_HISTORY -> {
                if(isToChangeMediaItems || radioSource.isStationsFromHistoryUpdated){
                    radioSource.isStationsFromHistoryUpdated = false
                    exoPlayer.setMediaItems(radioSource.stationsFromHistoryMediaItems)
                }
            }

            SEARCH_FROM_HISTORY_ONE_DATE -> {
                if(isToChangeMediaItems || RadioSource.isStationsFromHistoryOneDateUpdated){
                    RadioSource.isStationsFromHistoryOneDateUpdated = false
                    exoPlayer.setMediaItems(RadioSource.stationsFromHistoryOneDateMediaItems)
                }
            }

            SEARCH_FROM_RECORDINGS -> {
                if(isToChangeMediaItems || isStationsFromRecordingUpdated){
                    isStationsFromRecordingUpdated = false
                    exoPlayer.setMediaItems(stationsFromRecordingsMediaItems)

                }
            }
        }
    }


    private fun adjustIndexFromHistory(index : Int) : Int {

        return if(index < radioSource.allHistoryMap[0])
            index -1
        else {

            var shift = 3

            for(i in 1 until radioSource.allHistoryMap.size){

                if( index < radioSource.allHistoryMap[i])
                    break
                else shift += 2
            }
            index - shift
        }


    }

    override fun onTaskRemoved(rootIntent: Intent?) {
            super.onTaskRemoved(rootIntent)

        canOnDestroyBeCalled = true

        if(isToKillServiceOnAppClose){
            exoPlayer.stop()
        }

        if(!exoPlayer.isPlaying){

            Log.d("CHECKTAGS", "not playing")

//            NotificationManagerCompat.from(this@RadioService).cancel(RECORDING_NOTIFICATION_ID)

            radioNotificationManager.removeNotification()

        }
    }



    override fun onDestroy() {

        unregisterNewDateReceiver()

        mediaSessionConnector.setPlayer(null)
        mediaSessionConnector.setQueueNavigator(null)
        mediaSessionConnector.setPlaybackPreparer(null)

        if(canOnDestroyBeCalled){
            Log.d("CHECKTAGS", "on destroy")
            mediaSession.run {
                isActive = false
                release()
            }


//            NotificationManagerCompat.from(this@RadioService).cancel(RECORDING_NOTIFICATION_ID)
            radioSource.subscribeToFavouredStations.removeObserver(observerForDatabase)
            radioSource.allRecordingsLiveData.removeObserver(observerForRecordings)

            exoRecord.removeExoRecordListener("MainListener")



            exoPlayer.removeListener(radioPlayerEventListener)
            exoPlayer.release()


            serviceJob.cancel()

            serviceScope.cancel()
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

//    override fun onBind(intent: Intent?): IBinder? {
//        Log.d("CHECKTAGS", "intent:  ${ intent?.action }")
//        return super.onBind(intent)
//    }
//
//    override fun onUnbind(intent: Intent?): Boolean {
//        Log.d("CHECKTAGS", "intent unbind:  ${ intent?.action }")
//        return super.onUnbind(intent)
//    }
//
//    override fun onRebind(intent: Intent?) {
//        Log.d("CHECKTAGS", "intent rebind:  ${ intent?.action }")
//        super.onRebind(intent)
//    }

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


    fun insertRadioStation(station : RadioStation) = serviceScope.launch(Dispatchers.IO){

        databaseRepository.insertRadioStation(station)

    }

    private fun initialHistoryPref(){
        historyDatesPref = historySettingsPref.getInt(HISTORY_PREF_DATES, HISTORY_DATES_PREF_DEFAULT
        )
        historyPrefBookmark = historySettingsPref.getInt(HISTORY_PREF_BOOKMARK, HISTORY_BOOKMARK_PREF_DEFAULT)
    }


    private var isLastDateUpToDate = true


    private fun getLastDateAndCheck() = serviceScope.launch {

        val date = databaseRepository.getLastDate()
        date?.let {
            initialDate = it.date
            currentDateLong = it.time
        }

        val newTime = System.currentTimeMillis()
        calendar.time = Date(newTime)
        val update = Utils.fromDateToString(calendar)

        if(update != initialDate){
            initialDate = update
            currentDateLong = newTime
            isLastDateUpToDate = false
        }
    }


    fun checkDateAndUpdateHistory(stationID: String) = serviceScope.launch(Dispatchers.IO) {

        if(!isLastDateUpToDate){
            isLastDateUpToDate = true
            compareDatesWithPrefAndCLeanIfNeeded(HistoryDate(initialDate, currentDateLong))
        }
        databaseRepository.insertStationDateCrossRef(StationDateCrossRef(stationID, initialDate))

    }


    private fun compareDatesWithPrefAndCLeanIfNeeded(newDate: HistoryDate?)
            = serviceScope.launch(Dispatchers.IO) {

        newDate?.let {
            databaseRepository.insertNewDate(newDate)
        }


        val numberOfDatesInDB =  databaseRepository.getNumberOfDates()

        if(historyDatesPref >= numberOfDatesInDB) return@launch
        else {
            isCleanUpNeeded = true
            val numberOfDatesToDelete = numberOfDatesInDB - historyDatesPref
            val deleteList = databaseRepository.getDatesToDelete(numberOfDatesToDelete)

            deleteList.forEach {
                databaseRepository.deleteAllCrossRefWithDate(it.date)
                databaseRepository.deleteDate(it)
                databaseRepository.deleteTitlesWithDate(it.time)
            }
        }
    }


    private val newDateIntentFilter = IntentFilter().apply {
        addAction(Intent.ACTION_DATE_CHANGED)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
    }


    private val newDateReceiver = NewDateReceiver(){

        val newTime = System.currentTimeMillis()
        calendar.time = Date(newTime)
        val update = Utils.fromDateToString(calendar)

        if(update != initialDate){
            initialDate = update
            currentDateLong = newTime
            isLastDateUpToDate = false
        }
    }

    private fun registerNewDateReceiver(){
       registerReceiver(newDateReceiver, newDateIntentFilter)
    }

    private fun unregisterNewDateReceiver(){
        unregisterReceiver(newDateReceiver)
    }


}