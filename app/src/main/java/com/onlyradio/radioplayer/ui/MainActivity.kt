package com.onlyradio.radioplayer.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.RequestManager
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.data.local.entities.Recording
import com.onlyradio.radioplayer.databinding.ActivityMainBinding
import com.onlyradio.radioplayer.databinding.StubPlayerActivityMainBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.ui.animations.LoadingAnim
import com.onlyradio.radioplayer.ui.delegates.Navigation
import com.onlyradio.radioplayer.ui.delegates.NavigationImpl
import com.onlyradio.radioplayer.ui.fragments.*
import com.onlyradio.radioplayer.ui.stubs.MainPlayerView
import com.onlyradio.radioplayer.ui.viewmodels.DatabaseViewModel
import com.onlyradio.radioplayer.ui.viewmodels.HistoryViewModel
import com.onlyradio.radioplayer.ui.viewmodels.MainViewModel
import com.onlyradio.radioplayer.ui.viewmodels.RecordingsViewModel
import com.onlyradio.radioplayer.ui.viewmodels.SettingsViewModel
import com.onlyradio.radioplayer.utils.Constants.TEXT_SIZE_STATION_TITLE_PREF
import com.onlyradio.radioplayer.utils.Constants.UPDATES_DOWNLOADED
import com.onlyradio.radioplayer.utils.Constants.UPDATES_DOWNLOADING
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val mainViewModel: MainViewModel by viewModels()
    val settingsViewModel : SettingsViewModel by viewModels()
    val recordingsViewModel : RecordingsViewModel by viewModels()
    val historyViewModel : HistoryViewModel by viewModels()
    val favViewModel : DatabaseViewModel by viewModels()

    lateinit var bind : ActivityMainBinding
    var bindPlayer : StubPlayerActivityMainBinding? = null

    private val separatorAnimation : LoadingAnim by lazy { LoadingAnim(this,
        bind.viewSeparatorStart!!, bind.viewSeparatorEnd!!,
        bind.separatorLowest!!, bind.separatorSecond!!) }


    private val animationIn : Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.fall_down) }
    val layoutAnimationController : LayoutAnimationController by lazy {
        LayoutAnimationController(animationIn).apply {
            delay = 0.1f
            order = LayoutAnimationController.ORDER_NORMAL
        }
    }

    private val navigation : Navigation by lazy {
        NavigationImpl(supportFragmentManager, mainViewModel)
    }

    private var playerUtils : MainPlayerView? = null


    @Inject
    lateinit var glide : RequestManager

    private var currentPlayingStation : RadioStation? = null
    private var currentPlayingRecording : Recording? = null


    private lateinit var appUpdateManager : AppUpdateManager
    private val updateType = AppUpdateType.FLEXIBLE

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->

        settingsViewModel.onInstallUpdateStatus(state)

    }

    companion object{
        var uiMode = 0
        var flHeight = 0
    }




    override fun onBackPressed() {

        if(mainViewModel.isInDetailsFragment.value == false) {
            this.moveTaskToBack(true)
        }  else {
            navigation.handleNavigationToFragments(bind.bottomNavigationView.selectedItemId)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        appUpdateManager.registerListener(installStateUpdatedListener)
        checkForUpdates()

        setTheme(R.style.Theme_RadioPlayer)

        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.stubPlayer.setOnInflateListener{ _, view ->
            bindPlayer = StubPlayerActivityMainBinding.bind(view)
            playerUtils = MainPlayerView(bindPlayer!!, glide, resources.getString(R.string.time_left))
            callPlayerStubRelatedMethods()
        }

//        window.navigationBarColor = ContextCompat.getColor(this, R.color.toolbar)

        uiMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        observeIsToPlayLoadingAnim()

        refreshSeparators()

        navigation.initialNavigation()

        observeNewStation()

        observeRecordingDuration()

        setOnBottomNavClickListener()

        setOnBottomNavItemReselect()

        observeUpdatesState()

            bind.root.doOnLayout {
                flHeight = bind.viewHeight.height
            }
    }


    private fun checkForUpdates(){

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info->
            settingsViewModel.onUpdatesSuccessListener(info)
        }
    }


    private fun initializeUpdate(){
        appUpdateManager.startUpdateFlowForResult(
            settingsViewModel.updateInfo, updateType, this, 123
        )
    }

    private fun observeUpdatesState(){

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                settingsViewModel.updatesStatus.collectLatest { status ->

                    when(status){

                        UPDATES_DOWNLOADING -> {
                            Toast.makeText(applicationContext, resources.getString(R.string.update_downloading),
                                Toast.LENGTH_SHORT).show()
                        }

                        UPDATES_DOWNLOADED -> {
                            Toast.makeText(applicationContext, resources.getString(R.string.update_downloaded),
                                Toast.LENGTH_SHORT).show()
                            lifecycleScope.launch {
                                delay(2000)
                                appUpdateManager.completeUpdate()
                            }
                        }
                    }
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                settingsViewModel.updatesInitialize.collectLatest { _ ->
                    initializeUpdate()
                }
            }
        }
    }


    private fun observeRecordingDuration(){

        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED){

                recordingsViewModel.durationWithPosition.collectLatest {
                    playerUtils?.updateRecordingDuration(it)
                }
            }
        }
    }



    private fun observeIsInDetails(){

        mainViewModel.isInDetailsFragment.observe(this){
            if(it){
                bindPlayer?.tvExpandHideText?.setText(R.string.Hide)
            } else {
                bindPlayer?.tvExpandHideText?.setText(R.string.Expand)
            }
        }
    }

    private fun observeIsToPlayLoadingAnim(){

        mainViewModel.isToPlayLoadAnim.observe(this){

            if(uiMode == Configuration.UI_MODE_NIGHT_YES){
                if(it) separatorAnimation.startLoadingAnim()
                else separatorAnimation.endLoadingAnim()
            } else {
                if(!it || mainViewModel.isNewSearch)
                    bind.progressBarBottom?.hide()
                else bind.progressBarBottom?.show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.connectMediaBrowser()
    }


    private fun refreshSeparators(){
        if(!mainViewModel.isInitialLaunchOfTheApp && uiMode == Configuration.UI_MODE_NIGHT_YES){
            separatorAnimation.refresh()
        }
    }


    private fun handleStubPlayer(){

        bindPlayer?.let {
            if(it.root.visibility == View.GONE)
                playerUtils?.slideInPlayer()
        } ?: kotlin.run {
            bind.stubPlayer.inflate()
        }

        playerUtils?.updateImage()

    }


    private fun observeNewStation(){

        RadioService.currentPlayingStation.observe(this){ station ->

            currentPlayingStation = station

            handleStubPlayer()

        }


        RadioService.currentPlayingRecording.observe(this){ recording ->

            currentPlayingRecording = recording

            handleStubPlayer()

        }
    }

    private fun callPlayerStubRelatedMethods (){

        playerUtils?.slideInPlayer()

        clickListenerToHandleNavigationWithDetailsFragment()

        onClickListenerForTogglePlay()

        observePlaybackStateToChangeIcons()

        observeCurrentSongTitle()

        observePlayerBufferingState()

        observeIsInDetails()

    }

    private fun observePlayerBufferingState(){
        mainViewModel.isPlayerBuffering.observe(this){
            bindPlayer?.progressBuffer?.isVisible = it
        }
    }

    private fun observeCurrentSongTitle (){

        mainViewModel.currentSongTitle.observe(this){ title ->

            playerUtils?.handleTitleText(title)
        }
    }


    private fun setOnBottomNavClickListener(){

        bind.bottomNavigationView.setOnItemSelectedListener {
            navigation.handleNavigationToFragments(it.itemId)
        }
    }

    private fun setOnBottomNavItemReselect(){
        bind.bottomNavigationView.setOnItemReselectedListener {
            if(mainViewModel.isInDetailsFragment.value == true){
                navigation.handleNavigationToFragments(it.itemId)
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun clickListenerToHandleNavigationWithDetailsFragment(){

        bindPlayer?.tvStationTitle?.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_DOWN){
                navigation.handleNavigationWithDetailsFragment(bind.bottomNavigationView.selectedItemId)
            }
            true
        }
    }



    private fun onClickListenerForTogglePlay(){

        bindPlayer?.ivTogglePlayCurrentStation?.setOnClickListener { _ ->

            if(RadioService.isFromRecording){

                currentPlayingRecording?.let { recordingsViewModel.playOrToggleRecording(rec = it) }

            } else {

                currentPlayingStation?.let { mainViewModel.playOrToggleStation(
                    it,
                    isToChangeMediaItems = false,
                    searchFlag = RadioService.currentMediaItems
                ) }
            }
        }
    }


    private fun observePlaybackStateToChangeIcons (){

        mainViewModel.playbackState.observe(this){

            playerUtils?.handleIcons(it)

        }
    }



    override fun onStop() {
        super.onStop()
        this.cacheDir.deleteRecursively()

        mainViewModel.saveSearchPrefs()

        settingsViewModel.textSizePref.edit()
            .putFloat(TEXT_SIZE_STATION_TITLE_PREF, settingsViewModel.stationsTitleSize).apply()

    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }
}

