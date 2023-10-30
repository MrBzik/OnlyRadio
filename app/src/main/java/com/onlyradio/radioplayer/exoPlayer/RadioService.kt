@file:Suppress("DEPRECATION")

package com.onlyradio.radioplayer.exoPlayer

import android.animation.ValueAnimator
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.Virtualizer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.media.MediaBrowserServiceCompat
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.data.local.entities.Recording
import com.onlyradio.radioplayer.exoPlayer.callbacks.RadioPlaybackPreparer
import com.onlyradio.radioplayer.exoPlayer.callbacks.RadioPlayerEventListener
import com.onlyradio.radioplayer.exoPlayer.callbacks.RadioPlayerNotificationListener
import com.onlyradio.radioplayer.repositories.FavRepo
import com.onlyradio.radioplayer.ui.fragments.FavStationsFragment
import com.onlyradio.radioplayer.utils.Commands.COMMAND_CHANGE_REVERB_MODE
import com.onlyradio.radioplayer.utils.Commands.COMMAND_CLEAR_MEDIA_ITEMS
import com.onlyradio.radioplayer.utils.Commands.COMMAND_NEW_SEARCH
import com.onlyradio.radioplayer.utils.Commands.COMMAND_ON_DROP_STATION_IN_PLAYLIST
import com.onlyradio.radioplayer.utils.Commands.COMMAND_REMOVE_MEDIA_ITEM
import com.onlyradio.radioplayer.utils.Commands.COMMAND_REMOVE_RECORDING_MEDIA_ITEM
import com.onlyradio.radioplayer.utils.Commands.COMMAND_RESTART_PLAYER
import com.onlyradio.radioplayer.utils.Commands.COMMAND_RESTORE_RECORDING_MEDIA_ITEM
import com.onlyradio.radioplayer.utils.Commands.COMMAND_START_RECORDING
import com.onlyradio.radioplayer.utils.Commands.COMMAND_STOP_RECORDING
import com.onlyradio.radioplayer.utils.Commands.COMMAND_TOGGLE_REVERB
import com.onlyradio.radioplayer.utils.Commands.COMMAND_UPDATE_FAV_PLAYLIST
import com.onlyradio.radioplayer.utils.Commands.COMMAND_UPDATE_HISTORY_MEDIA_ITEMS
import com.onlyradio.radioplayer.utils.Commands.COMMAND_UPDATE_HISTORY_ONE_DATE_MEDIA_ITEMS
import com.onlyradio.radioplayer.utils.Commands.COMMAND_UPDATE_RADIO_PLAYBACK_PITCH
import com.onlyradio.radioplayer.utils.Commands.COMMAND_UPDATE_RADIO_PLAYBACK_SPEED
import com.onlyradio.radioplayer.utils.Commands.COMMAND_UPDATE_REC_PLAYBACK_SPEED
import com.onlyradio.radioplayer.utils.Constants.BUFFER_FOR_PLAYBACK
import com.onlyradio.radioplayer.utils.Constants.BUFFER_PREF
import com.onlyradio.radioplayer.utils.Constants.BUFFER_SIZE_IN_MILLS
import com.onlyradio.radioplayer.utils.Constants.FOREGROUND_PREF
import com.onlyradio.radioplayer.utils.Constants.IS_ADAPTIVE_LOADER_TO_USE
import com.onlyradio.radioplayer.utils.Constants.IS_NEW_SEARCH
import com.onlyradio.radioplayer.utils.Constants.IS_TO_CLEAR_HISTORY_ITEMS
import com.onlyradio.radioplayer.utils.Constants.ITEM_INDEX
import com.onlyradio.radioplayer.utils.Constants.MEDIA_ROOT_ID
import com.onlyradio.radioplayer.utils.Constants.NO_ITEMS
import com.onlyradio.radioplayer.utils.Constants.NO_PLAYLIST
import com.onlyradio.radioplayer.utils.Constants.RECONNECT_PREF
import com.onlyradio.radioplayer.utils.Constants.RECORDING_AUTO_STOP_PREF
import com.onlyradio.radioplayer.utils.Constants.RECORDING_NAMING_PREF
import com.onlyradio.radioplayer.utils.Constants.RECORDING_QUALITY_PREF
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_API
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_HISTORY_ONE_DATE
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_LAZY_LIST
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.onlyradio.radioplayer.utils.Constants.TITLE_UNKNOWN
import com.onlyradio.radioplayer.utils.Utils
import com.onlyradio.radioplayer.utils.setPreset
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_MAX_BUFFER_MS
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.audio.AuxEffectInfo
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource.Factory
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.onlyradio.radioplayer.data.models.OnRestoreMediaItem
import com.onlyradio.radioplayer.data.models.OnSnackRestore
import com.onlyradio.radioplayer.exoRecord.ExoRecord
import com.onlyradio.radioplayer.extensions.makeToast
import com.onlyradio.radioplayer.repositories.BookmarksRepo
import com.onlyradio.radioplayer.repositories.DatesRepo
import com.onlyradio.radioplayer.repositories.LazyRepo
import com.onlyradio.radioplayer.repositories.TitlesRepo
import com.onlyradio.radioplayer.utils.Commands.COMMAND_ON_SWIPE_DELETE
import com.onlyradio.radioplayer.utils.Commands.COMMAND_ON_SWIPE_RESTORE
import com.onlyradio.radioplayer.utils.Constants.ITEM_ID
import com.onlyradio.radioplayer.utils.Constants.ITEM_PLAYLIST
import com.onlyradio.radioplayer.utils.Constants.ITEM_PLAYLIST_NAME
import com.onlyradio.radioplayer.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.sql.Date
import java.util.Calendar
import javax.inject.Inject


private const val SERVICE_TAG = "service tag"


@AndroidEntryPoint
class RadioService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory : Factory

    @Inject
    lateinit var radioServiceConnection: RadioServiceConnection

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

    val exoRecordImp by lazy {
        ExoRecordImpl(this)
    }

    @Inject
    lateinit var favRepo: FavRepo

    @Inject
    lateinit var bookmarksRepo: BookmarksRepo

    @Inject
    lateinit var titlesRepo: TitlesRepo

    @Inject
    lateinit var datesRepo : DatesRepo

    @Inject
    lateinit var lazyRepo : LazyRepo


    private val serviceJob = SupervisorJob()

    val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    val dbOperators by lazy {
        DBOperators(this)
    }

    lateinit var radioNotificationManager: RadioNotificationManager

    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var mediaSessionConnector : MediaSessionConnector

    private lateinit var radioPlayerEventListener: RadioPlayerEventListener

    var isForegroundService = false

    var isPlaybackStatePlaying = false

    private var isPlayerInitialized = false

    val calendar = Calendar.getInstance()

    private var isFavPlaylistPendingUpdate = false

    private var favStationsTemp : List<RadioStation> = emptyList()

    var stationsFromRecordings = mutableListOf<Recording>()
    private var stationsFromRecordingsMediaItems = mutableListOf<MediaItem>()
//    var isStationsFromRecordingUpdated = true

    private val observerForRecordings = Observer<List<Recording>>{

        if(currentMediaItems != SEARCH_FROM_RECORDINGS){
            stationsFromRecordings = it.toMutableList()
            val fileDir = this@RadioService.filesDir.path
            stationsFromRecordingsMediaItems = it.map { rec ->
                MediaItem.fromUri("$fileDir/${rec.id}")
            }.toMutableList()
//            isStationsFromRecordingUpdated = true
        }



//        radioSource.isRecordingUpdated.postValue(true)
    }

     var currentRadioStation: RadioStation? = null
     var currentRecording : Recording? = null

    var lastInsertedSong = ""

    companion object{

        var historyPrefBookmark = 20

        var currentPlaylistName = ""
        var selectedHistoryDate = -1L

        var lastDeletedStation : RadioStation? = null
        var lastDeletedRecording : Recording? = null


        var currentlyPlayingSong = TITLE_UNKNOWN
//        var currentStation : MediaMetadataCompat? = null
        var currentMediaItems = NO_ITEMS

        var isInStationDetails = false

        var currentPlayingStation : MutableLiveData<RadioStation> = MutableLiveData()
        var currentPlayingRecording : MutableLiveData<Recording> = MutableLiveData()
//        var currentPlayingItemPosition = -1

        var isFromRecording = false

//        var canOnDestroyBeCalled = false

        var isToKillServiceOnAppClose = false

        var currentDateLong : Long = 0
        var currentDateString = ""

        val currentSongTitle = MutableLiveData<String>()

        val recordingPlaybackPosition = MutableLiveData<Long>()
        val recordingDuration = MutableLiveData<Long>()
        val recordingDurationRemains = MutableLiveData<Long>()

        var playbackSpeedRec = 100
        var playbackSpeedRadio = 100
        var playbackPitchRadio = 100
        var isSpeedPitchLinked = true

        var isToReconnect = true

        var isToUpdateLiveData = true

        var bufferSizeInMills = 0
//        var bufferSizeInBytes = 0
        var bufferForPlayback = 0
//        var isToSetBufferInBytes = false
        var isAdaptiveLoaderToUse = false

        var reverbMode = 0

        var isVirtualizerEnabled = false

        var autoStopRec = 0
        var isToUseTitleForRecNaming = false
    }

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

        mediaSession = initializeMediaSession()

        sessionToken = mediaSession.sessionToken

        radioNotificationManager = initializeNotificationManager()

        val radioPlaybackPreparer = initializePlaybackPreparer()

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlaybackPreparer(radioPlaybackPreparer)
            setQueueNavigator(RadioQueueNavigator())
        }

        radioPlayerEventListener = RadioPlayerEventListener(this)

        exoRecordImp.checkRecordingAndRecoverIfNeeded()

        initialPrefChecks()

        exoPlayer = provideExoPlayer()
        mediaSessionConnector.setPlayer(exoPlayer)
        radioNotificationManager.showNotification(exoPlayer)

        registerNewDateReceiver()

        collectFavStations()

        radioSource.allRecordingsLiveData.observeForever(observerForRecordings)

        dbOperators.getLastDateAndCheck()

        radioServiceConnection.setterForPlayerPos {
            exoPlayer.currentMediaItemIndex
        }
    }


    private fun initialPrefChecks(){

        initialCheckForBuffer()

        isToReconnect = this@RadioService
            .getSharedPreferences(RECONNECT_PREF, Context.MODE_PRIVATE).getBoolean(
                RECONNECT_PREF, true)

        isToKillServiceOnAppClose = this@RadioService.getSharedPreferences(
            FOREGROUND_PREF, Context.MODE_PRIVATE).getBoolean(FOREGROUND_PREF, false)

        dbOperators.initialHistoryPref()


        this@RadioService.getSharedPreferences(
            RECORDING_QUALITY_PREF, Context.MODE_PRIVATE).apply {

            autoStopRec = getInt(RECORDING_AUTO_STOP_PREF, 180) * 60000
            isToUseTitleForRecNaming = getBoolean(RECORDING_NAMING_PREF, false)

        }
    }

    private fun collectFavStations(){
        serviceScope.launch(Dispatchers.IO) {
            radioSource.subscribeToFavouredStations.collectLatest {
                if(currentMediaItems == SEARCH_FROM_FAVOURITES){
                    if(isInStationDetails)
                        isFavPlaylistPendingUpdate = true
                    favStationsTemp = it
                } else {
                    radioSource.stationsFavoured = it.toMutableList()
                    radioSource.createMediaItemsFromDB(exoPlayer, currentRadioStation)
                }
            }
        }
    }

    private fun initializeMediaSession() : MediaSessionCompat{

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, FLAG_IMMUTABLE)
        }

        return MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
    }

    private fun initializeNotificationManager() : RadioNotificationManager {
        return RadioNotificationManager(
            context = this,
            sessionToken = mediaSession.sessionToken,
            notificationListener = RadioPlayerNotificationListener(this),
            glide = glide,
            handleBookmark = { dbOperators.upsertNewBookmark() },
            handleStopRecording = { exoRecordImp.stopRecording() }
        )
    }

    private fun initializePlaybackPreparer() : RadioPlaybackPreparer {
        return RadioPlaybackPreparer(
            radioSource = radioSource,
            playerPrepared = { flag, playWhenReady, itemIndex, isToChangeMediaItems, isSameStation ->

                var index = itemIndex

                val isFromRecordings = flag == SEARCH_FROM_RECORDINGS

                if(flag == SEARCH_FROM_HISTORY && !isInStationDetails){
                    index = adjustIndexFromHistory(itemIndex)
                } else if (flag == SEARCH_FROM_HISTORY_ONE_DATE && !isInStationDetails) {
                    index --
                }

                preparePlayer(
                    playNow = playWhenReady,
                    itemIndex =  index,
                    isToChangeMediaItems = isToChangeMediaItems,
                    isFromRecordings = isFromRecordings,
                    isSameStation = isSameStation
                )

            },
            onCommand = {
                    command, extras ->

                when(command){

                    COMMAND_NEW_SEARCH -> {

                        val isNewSearch = extras?.getBoolean(IS_NEW_SEARCH) ?: false

                        searchRadioStations(isNewSearch)

                    }


                    COMMAND_START_RECORDING -> {

                        if(isPlaybackStatePlaying){

                            exoRecordImp.onCommandStartRecording()
                        }
                    }

                    COMMAND_STOP_RECORDING -> {
                        exoRecordImp.stopRecording()
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

                        updateSpeedPitch(isSpeed = true)
                    }

                    COMMAND_UPDATE_RADIO_PLAYBACK_PITCH -> {

                        updateSpeedPitch(isSpeed = false)
                    }

                    COMMAND_RESTART_PLAYER -> {
                        recreateExoPlayer()
                    }

                    COMMAND_CHANGE_REVERB_MODE -> {
                        changeReverbMode()
                    }

                    COMMAND_TOGGLE_REVERB -> {
                        changeVirtualizerLevel()
                    }

//                COMMAND_COMPARE_DATES_PREF_AND_CLEAN -> {
//                    compareDatesWithPrefAndCLeanIfNeeded()
//                }

                    COMMAND_UPDATE_FAV_PLAYLIST -> {

                        if(isFavPlaylistPendingUpdate){
                            isFavPlaylistPendingUpdate = false

                            radioSource.stationsFavoured = favStationsTemp.toMutableList()
                            favStationsTemp = emptyList()

                            clearMediaItems(false)

                            radioSource.createMediaItemsFromDB(exoPlayer, currentRadioStation)

                        }
                    }


                    COMMAND_REMOVE_RECORDING_MEDIA_ITEM ->{

                        val index = extras?.getInt(ITEM_INDEX)


                        index?.let{ pos ->

                            lastDeletedRecording = stationsFromRecordings[pos]
                            stationsFromRecordings.removeAt(pos)
                            stationsFromRecordingsMediaItems.removeAt(pos)


                            if(pos == exoPlayer.currentMediaItemIndex) {
//                                currentPlayingItemPosition = -1
                                currentMediaItems = NO_ITEMS
                                exoPlayer.stop()
                                exoPlayer.clearMediaItems()


                            } else {

//                                if(pos < exoPlayer.currentMediaItemIndex)
//                                    currentPlayingItemPosition --
                                exoPlayer.removeMediaItem(pos)
                            }
                        }
                    }


                    COMMAND_RESTORE_RECORDING_MEDIA_ITEM -> {

                        val index = extras?.getInt(ITEM_INDEX)

                        index?.let { pos ->

//                            if(pos <= exoPlayer.currentMediaItemIndex)
//                                currentPlayingItemPosition ++

                            lastDeletedRecording?.let { rec ->
                                stationsFromRecordings.add(pos, rec)

                                val path = this@RadioService.filesDir.absolutePath + "/" + rec.id

                                val mediaItem = MediaItem.fromUri(path)

                                stationsFromRecordingsMediaItems.add(
                                    pos, mediaItem
                                )
                                exoPlayer.addMediaItem(pos, mediaItem)
                            }
                        }
                    }


                    COMMAND_ON_SWIPE_DELETE -> {
                        onSwipeDelete(extras)
                    }

                    COMMAND_ON_SWIPE_RESTORE -> {
                        onSwipeRestore()
                    }


                    COMMAND_REMOVE_MEDIA_ITEM -> {

                        val index = extras?.getInt(ITEM_INDEX, -1) ?: -1

                        onRemoveMediaItem(index)

                    }
//
//                    COMMAND_RESTORE_MEDIA_ITEM -> {
//
//                        val index = extras?.getInt(ITEM_INDEX, -1) ?: -1
//
//                        onRestoreMediaItem(index)
//
//
//                    }

                    COMMAND_ON_DROP_STATION_IN_PLAYLIST -> {


                        val radioStation =  FavStationsFragment.dragAndDropStation

                        radioStation?.let { station ->

//                            currentPlayingItemPosition ++
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

                        val isToClearMediaItems = extras?.getBoolean(IS_TO_CLEAR_HISTORY_ITEMS) ?: true

                        if(isToClearMediaItems){

                            clearMediaItems(false)

                            if(radioSource.stationsFromHistoryMediaItems.size > 1){
                                for(i in 1 until radioSource.stationsFromHistoryMediaItems.size){
                                    exoPlayer.addMediaItem(
                                        radioSource.stationsFromHistoryMediaItems[i]
                                    )
                                }
                            }
                        } else {

                            for(i in exoPlayer.mediaItemCount until radioSource.stationsFromHistoryMediaItems.size){
                                exoPlayer.addMediaItem(
                                    radioSource.stationsFromHistoryMediaItems[i]
                                )
                            }
                        }
//                    radioSource.isStationsFromHistoryUpdated = false
                    }


                    COMMAND_UPDATE_HISTORY_ONE_DATE_MEDIA_ITEMS -> {

//                        currentPlayingItemPosition = 0

                        clearMediaItems(false)

                        if(RadioSource.stationsFromHistoryOneDateMediaItems.size > 1){
                            for(i in 1 until RadioSource.stationsFromHistoryOneDateMediaItems.size){
                                exoPlayer.addMediaItem(
                                    RadioSource.stationsFromHistoryOneDateMediaItems[i]
                                )
                            }
                        }
                    }
                }
            }
        )
    }


    private fun saveDeletedItem(index : Int, playlist: Int){
        lastDeletedStation = when(playlist){
            SEARCH_FROM_FAVOURITES -> radioSource.stationsFavoured[index]
            SEARCH_FROM_PLAYLIST -> RadioSource.stationsInPlaylist[index]
            else -> null
        }
    }


    private fun onRemoveMediaItem(index : Int){

        if(index != -1){

            if(index == exoPlayer.currentMediaItemIndex){

                clearMediaItems(true)

            } else {
                exoPlayer.removeMediaItem(index)

                if(currentMediaItems == SEARCH_FROM_FAVOURITES){
//                    lastDeletedStation = radioSource.stationsFavoured[index]
                    radioSource.stationsFavoured.removeAt(index)
                    radioSource.stationsFavouredMediaItems.removeAt(index)

                } else if(currentMediaItems == SEARCH_FROM_PLAYLIST){
//                    lastDeletedStation = RadioSource.stationsInPlaylist[index]
                    RadioSource.stationsInPlaylist.removeAt(index)
                    RadioSource.stationsInPlaylistMediaItems.removeAt(index)

                }
            }
        }
    }


    private fun onRestoreMediaItem(index: Int) {

        if(index != -1){

            lastDeletedStation?.let { station ->
                val mediaItem = MediaItem.fromUri(station.url!!)
                exoPlayer.addMediaItem(index, mediaItem)

                when (currentMediaItems) {
                    SEARCH_FROM_FAVOURITES -> {
                        radioSource.stationsFavoured.add(index, station)
                        radioSource.stationsFavouredMediaItems.add(index, mediaItem)

                    }
                    SEARCH_FROM_PLAYLIST -> {
                        RadioSource.stationsInPlaylist.add(index, station)
                        RadioSource.stationsInPlaylistMediaItems.add(index, mediaItem)
                    }
                }
            }
        }
    }

    private lateinit var onRestoreMediaItem : OnRestoreMediaItem


    private fun onSwipeDelete(extras : Bundle?) = serviceScope.launch {

        val index = extras?.getInt(ITEM_INDEX, -1) ?: -1

        val playlist = extras?.getInt(ITEM_PLAYLIST, 0) ?: 0

        val playlistName = extras?.getString(ITEM_PLAYLIST_NAME, "") ?: ""

        val stationID = extras?.getString(ITEM_ID) ?: ""

        onRestoreMediaItem = OnRestoreMediaItem(
            index = index,
            playlist = playlist,
            playlistName = playlistName,
            stationId = stationID
        )

        saveDeletedItem(index = index, playlist = playlist)

        if(playlist == currentMediaItems){
            if(playlist != SEARCH_FROM_PLAYLIST || playlistName == currentPlaylistName){
                onRemoveMediaItem(index)
            }
        }

        when(playlist){

            SEARCH_FROM_FAVOURITES -> {
                favRepo.updateIsFavouredState(0, stationID)
            }

            SEARCH_FROM_PLAYLIST -> {
                onRestoreMediaItem.timeOfInsertion = favRepo.getTimeOfStationPlaylistInsertion(stationID, playlistName)
                favRepo.decrementInPlaylistsCount(stationID)
                favRepo.deleteStationPlaylistCrossRef(stationID, playlistName)
            }


            SEARCH_FROM_LAZY_LIST -> {
                RadioSource.removeItemFromLazyList(index)
                lazyRepo.setRadioStationPlayedDuration(stationID, 0)
            }
        }

        radioServiceConnection.onSwipeHandled.send(OnSnackRestore(playlist, playlistName))

    }


    private fun onSwipeRestore() = serviceScope.launch {

        val playlist = onRestoreMediaItem.playlist
        val playlistName = onRestoreMediaItem.playlistName
        val stationID = onRestoreMediaItem.stationId
        val index = onRestoreMediaItem.index

        if(playlist == currentMediaItems){
            if(playlist != SEARCH_FROM_PLAYLIST || playlistName == currentPlaylistName){
                onRestoreMediaItem(index)
            }
        }


        when(playlist){

            SEARCH_FROM_FAVOURITES -> {

                favRepo.updateIsFavouredState(lastDeletedStation?.favouredAt ?: 0, stationID)
            }

            SEARCH_FROM_PLAYLIST -> {

                favRepo.incrementInPlaylistsCount(stationID)
                favRepo.insertStationPlaylistCrossRef(StationPlaylistCrossRef(
                    stationID, playlistName, onRestoreMediaItem.timeOfInsertion
                ))

            }

            SEARCH_FROM_LAZY_LIST -> {
                RadioSource.restoreItemFromLazyList(index)
                lazyRepo.setRadioStationPlayedDuration(stationID, lastDeletedStation?.playDuration ?: 0)
            }
        }

    }


    private fun updateSpeedPitch(isSpeed : Boolean){

        if(!isFromRecording){
            val isToPlay = isPlaybackStatePlaying
            exoPlayer.pause()
            if(isSpeedPitchLinked){
                if(isSpeed) playbackPitchRadio = playbackSpeedRadio
                else playbackSpeedRadio = playbackPitchRadio
            }
            val params = PlaybackParameters(
                playbackSpeedRadio.toFloat()/100,
                playbackPitchRadio.toFloat()/100
            )
            exoPlayer.playbackParameters = params
            exoPlayer.playWhenReady = isToPlay
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


    private fun changeReverbMode() {

        try {
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
        } catch (e : Exception){
            reverbMode = 0
            this.makeToast(R.string.reverb_error)
        }
    }

    private var wasBassBoostSet = false

    private fun changeVirtualizerLevel() {

        if(!isVirtualizerEnabled){
            effectVirtualizer.enabled = false
        } else {

            effectVirtualizer.setStrength(666)

            if(!effectVirtualizer.enabled){
                effectVirtualizer.enabled = true
            }
        }

//        if(!wasBassBoostSet){
            exoPlayer.setAuxEffectInfo(AuxEffectInfo(effectVirtualizer.id, 1f))
//            wasBassBoostSet = true
//        }
    }




    private fun initialCheckForBuffer(){
        val buffPref = this@RadioService.getSharedPreferences(BUFFER_PREF, Context.MODE_PRIVATE)

        bufferSizeInMills = buffPref.getInt(BUFFER_SIZE_IN_MILLS, DEFAULT_MAX_BUFFER_MS)
//        bufferSizeInBytes = buffPref.getInt(BUFFER_SIZE_IN_BYTES, DEFAULT_TARGET_BUFFER_BYTES)
        bufferForPlayback = buffPref.getInt(BUFFER_FOR_PLAYBACK, 3000)
        if(bufferForPlayback % 1000 == 500){
            bufferForPlayback += 500
            buffPref.edit().putInt(BUFFER_FOR_PLAYBACK, bufferForPlayback).apply()
        }

//        isToSetBufferInBytes = buffPref.getBoolean(IS_TO_SET_BUFFER_IN_BYTES, false)
        isAdaptiveLoaderToUse = buffPref.getBoolean(IS_ADAPTIVE_LOADER_TO_USE, false)

    }



    private fun provideExoPlayer () : ExoPlayer {

//        val bites = if (!isToSetBufferInBytes) -1
//        else bufferSizeInBytes * 1024



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


    private fun recreateExoPlayer(){

        val isToPlay = exoPlayer.playWhenReady
        val itemIndex = exoPlayer.currentMediaItemIndex

        exoPlayer.stop()
        exoPlayer = provideExoPlayer()
        mediaSessionConnector.setPlayer(exoPlayer)
        radioNotificationManager.showNotification(exoPlayer)
        preparePlayer(
            playNow = isToPlay,
            itemIndex = itemIndex,
            isToChangeMediaItems = true,
            isFromRecordings = currentMediaItems == SEARCH_FROM_RECORDINGS,
            isSameStation = false
            )
    }


    private var isRecordingDurationListenerRunning = false

    fun listenToRecordDuration ()  {

//        val format = exoPlayer.audioFormat
//        val sampleRate = format?.sampleRate ?: 0
//        val channels = format?.channelCount ?: 0
//
//        Log.d("CHECKTAGS", "sampleRate is $sampleRate, channels : $channels")


        if(reverbMode != 0)
            exoPlayer.setAuxEffectInfo(AuxEffectInfo(environmentalReverb.id, 1f))

        if(isVirtualizerEnabled)
            exoPlayer.setAuxEffectInfo(AuxEffectInfo(effectVirtualizer.id, 1f))



        if(isFromRecording && !isRecordingDurationListenerRunning){

            isRecordingDurationListenerRunning = true

            serviceScope.launch {

                while (isFromRecording && isPlaybackStatePlaying){

                    val pos = mediaSession.controller.playbackState.currentPlaybackPosition

                    recordingPlaybackPosition.postValue(pos)

                    if(exoPlayer.duration > 0){
                        recordingDuration.postValue(exoPlayer.duration)

                        recordingDurationRemains.postValue(exoPlayer.duration - pos)

                        val delay = exoPlayer.duration - pos

                        if(delay > 600){
                            delay(500)
                        } else {
                            delay(delay)
                            exoPlayer.seekTo(0)
                            exoPlayer.pause()
                            recordingPlaybackPosition.postValue(0)
                            break
                        }
                    } else {
                        delay(500)
                    }

                }
                isRecordingDurationListenerRunning = false

            }
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
               return if(windowIndex != exoPlayer.currentMediaItemIndex){
                   MediaDescriptionCompat.Builder().build()
               } else {
                   val extra = Bundle()
                   extra.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentlyPlayingSong)
                   MediaDescriptionCompat.Builder()
                       .setExtras(extra)
                       .setIconUri(currentRadioStation?.favicon?.toUri())
                       .setTitle(currentRadioStation?.name)
                       .setSubtitle(currentlyPlayingSong)
                       .build()
               }
           } else{
               return try {
                   val index = minOf(windowIndex, stationsFromRecordings.lastIndex)

                   MediaDescriptionCompat.Builder()
                       .setIconUri(stationsFromRecordings[index].iconUri.toUri())
                       .setTitle(stationsFromRecordings[index].name)
                       .build()
               } catch (e : Exception){

                   MediaDescriptionCompat.Builder()
                       .setTitle("Some error occurred")
                       .build()
               }
           }
       }
   }


    private fun clearMediaItems(isNoList : Boolean = true){

        try{
            if(exoPlayer.currentMediaItemIndex > 0){

                exoPlayer.removeMediaItems(0, exoPlayer.currentMediaItemIndex)
            }

            if(exoPlayer.mediaItemCount > 1 && exoPlayer.currentMediaItemIndex == 0){
                exoPlayer.removeMediaItems(1, exoPlayer.mediaItemCount)
            }

            if(isNoList){
//                currentPlayingItemPosition = 0
                currentMediaItems = NO_PLAYLIST
            }

        } catch (e : Exception){
            throw IllegalArgumentException(
                "Strange crash from google I can't reproduce. " +
                        "\nExoplayer currentMediaItemIndex: ${exoPlayer.currentAdGroupIndex}" +
                        "Exoplayer mediaItemCount: ${exoPlayer.mediaItemCount}"
            )
        }
    }


    private fun preparePlayer(
        playNow : Boolean,
        itemIndex : Int = -1,
        isToChangeMediaItems : Boolean = true,
        isFromRecordings : Boolean = false,
        isSameStation : Boolean

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

            if(isToChangeMediaItems)
                updateMediaItems( currentMediaItems, isSameStation, itemIndex)


            if(!isSameStation){
                if(itemIndex >= 0 && exoPlayer.mediaItemCount > 0){
                    exoPlayer.seekToDefaultPosition(itemIndex)
                }
                exoPlayer.prepare()
            }

            exoPlayer.playWhenReady = playNow
        }


//        playFromUri(uri, playNow, itemIndex, isSamePlaylist)

      }


    private fun updateMediaItems(
        flag: Int,
        isSameStation: Boolean,
        itemIndex : Int
    ){

       val listOfMediaItems = when(flag){

            SEARCH_FROM_API -> {
                radioSource.stationsFromApiMediaItems
            }

            SEARCH_FROM_FAVOURITES -> {

                radioSource.stationsFavouredMediaItems

            }

            SEARCH_FROM_PLAYLIST -> {

                RadioSource.stationsInPlaylistMediaItems

            }

            SEARCH_FROM_HISTORY -> {

                radioSource.stationsFromHistoryMediaItems

            }
            SEARCH_FROM_HISTORY_ONE_DATE -> {

                RadioSource.stationsFromHistoryOneDateMediaItems

            }
            SEARCH_FROM_LAZY_LIST -> {
                RadioSource.lazyListMediaItems

            }
            SEARCH_FROM_RECORDINGS -> {
                stationsFromRecordingsMediaItems
            }
           else -> emptyList()
        }

        handleMediaItemsUpdate(listOfMediaItems, isSameStation, itemIndex)

    }


    private fun handleMediaItemsUpdate(items : List<MediaItem>, isSameStation: Boolean, index : Int){

        if(isSameStation){

            clearMediaItems(false)

            if(index == 0){
                if(items.size != 1)
                    exoPlayer.addMediaItems(items.subList(1, items.lastIndex))
            } else {

                exoPlayer.addMediaItems(0, items.subList(0, index))

                if(index < items.lastIndex){
                    exoPlayer.addMediaItems(items.subList(index + 1, items.lastIndex))
                }
            }

//            currentPlayingItemPosition = index

        } else {

            if(index != 0)
                isToIgnoreMediaItem = true

            exoPlayer.setMediaItems(items)
        }
    }

    var isToIgnoreMediaItem = false

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

//        canOnDestroyBeCalled = true

        radioServiceConnection.disconnectBrowser()

        if(isToKillServiceOnAppClose){

            dbOperators.savePlayDurationOnServiceKill(isPlaybackStatePlaying)

            exoPlayer.stop()
        }

        if(!exoPlayer.isPlaying){

//            NotificationManagerCompat.from(this@RadioService).cancel(RECORDING_NOTIFICATION_ID)

            radioNotificationManager.removeNotification()

        }
    }



    override fun onDestroy() {

        unregisterNewDateReceiver()

        mediaSessionConnector.setPlayer(null)
        mediaSessionConnector.setQueueNavigator(null)
        mediaSessionConnector.setPlaybackPreparer(null)

//        if(canOnDestroyBeCalled){
//            Log.d("CHECKTAGS", "on destroy")
            mediaSession.run {
                isActive = false
                release()
            }

//            NotificationManagerCompat.from(this@RadioService).cancel(RECORDING_NOTIFICATION_ID)

            radioSource.allRecordingsLiveData.removeObserver(observerForRecordings)

            exoRecord.removeExoRecordListener("MainListener")

            exoPlayer.removeListener(radioPlayerEventListener)
            exoPlayer.release()

            radioNotificationManager.clearServiceJob()
            serviceJob.cancel()

            serviceScope.cancel()
            android.os.Process.killProcess(android.os.Process.myPid())
//        }
    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
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

        if(update != currentDateString){
            currentDateString = update
            currentDateLong = newTime
            dbOperators.isLastDateUpToDate = false
        }
    }

    private fun registerNewDateReceiver(){
       registerReceiver(newDateReceiver, newDateIntentFilter)
    }

    private fun unregisterNewDateReceiver(){
        unregisterReceiver(newDateReceiver)
    }


}