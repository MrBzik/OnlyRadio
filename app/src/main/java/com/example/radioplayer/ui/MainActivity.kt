package com.example.radioplayer.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.databinding.ActivityMainBinding
import com.example.radioplayer.databinding.StubPlayerActivityMainBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.animations.DayNightAnim
import com.example.radioplayer.ui.animations.LoadingAnim
import com.example.radioplayer.ui.delegates.NavigationImpl
import com.example.radioplayer.ui.fragments.*
import com.example.radioplayer.ui.stubs.MainPlayerView
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.HistoryViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.ui.viewmodels.RecordingsViewModel
import com.example.radioplayer.ui.viewmodels.SettingsViewModel
import com.example.radioplayer.utils.Constants.TEXT_SIZE_STATION_TITLE_PREF
import dagger.hilt.android.AndroidEntryPoint
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

    private val navigation : NavigationImpl by lazy {
        NavigationImpl(supportFragmentManager, mainViewModel)
    }

    private val playerUtils : MainPlayerView by lazy {
        MainPlayerView(bindPlayer!!, glide)
    }

    private val dayNightAnim : DayNightAnim by lazy {
        DayNightAnim()
    }

    @Inject
    lateinit var glide : RequestManager

    private var currentPlayingStation : RadioStation? = null
    private var currentPlayingRecording : Recording? = null

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

        setTheme(R.style.Theme_RadioPlayer)

        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.stubPlayer.setOnInflateListener{ _, view ->
                bindPlayer = StubPlayerActivityMainBinding.bind(view)
        }

//        window.navigationBarColor = ContextCompat.getColor(this, R.color.toolbar)

        uiMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        observeIsToPlayLoadingAnim()

        navigation.initialNavigation()

        observeNewStation()

        setOnBottomNavClickListener()

        setOnBottomNavItemReselect()

        refreshSeparators()

            bind.root.doOnLayout {
                flHeight = bind.viewHeight.height
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


    fun smoothDayNightFadeOut(){

        dayNightAnim.smoothDayNightFadeOut(bind, bindPlayer)

    }

    fun smoothDayNightFadeIn(){

        dayNightAnim.smoothDayNightFadeIn(bind, bindPlayer)

    }

    private fun refreshSeparators(){
        if(!mainViewModel.isInitialLaunchOfTheApp && uiMode == Configuration.UI_MODE_NIGHT_YES){
            separatorAnimation.refresh()
        }
    }


    private fun handleStubPlayer(){

        bindPlayer?.let {
            if(it.root.visibility == View.GONE)
                playerUtils.slideInPlayer()
        } ?: kotlin.run {
            inflatePlayerStubAndCallRelatedMethods()
        }

        playerUtils.updateImage()

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

    private fun inflatePlayerStubAndCallRelatedMethods (){

        bind.stubPlayer.visibility = View.VISIBLE

        playerUtils.slideInPlayer()

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

            playerUtils.handleTitleText(title)
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

            playerUtils.handleIcons(it)

        }
    }



    override fun onStop() {
        super.onStop()
        this.cacheDir.deleteRecursively()

        mainViewModel.saveSearchPrefs()

        settingsViewModel.textSizePref.edit()
            .putFloat(TEXT_SIZE_STATION_TITLE_PREF, settingsViewModel.stationsTitleSize).apply()

    }


//    override fun onDestroy() {
//        mainViewModel.disconnectMediaBrowser()
//        Log.d("CHECKTAGS", "activity on destroy")
//        super.onDestroy()
//    }

}

