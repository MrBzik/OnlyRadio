package com.example.radioplayer.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import androidx.transition.Slide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.radioplayer.R
import com.example.radioplayer.data.models.PlayingItem
import com.example.radioplayer.databinding.ActivityMainBinding
import com.example.radioplayer.databinding.StubPlayerActivityMainBinding
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.animations.AlphaFadeOutAnim
import com.example.radioplayer.ui.animations.LoadingAnim
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.fragments.*
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.FAB_POSITION_X
import com.example.radioplayer.utils.Constants.FAB_POSITION_Y
import com.example.radioplayer.utils.Constants.IS_FAB_UPDATED
import com.example.radioplayer.utils.Constants.IS_NAME_EXACT
import com.example.radioplayer.utils.Constants.IS_SEARCH_FILTER_LANGUAGE
import com.example.radioplayer.utils.Constants.IS_TAG_EXACT
import com.example.radioplayer.utils.Constants.SEARCH_FULL_COUNTRY_NAME
import com.example.radioplayer.utils.Constants.SEARCH_PREF_COUNTRY
import com.example.radioplayer.utils.Constants.SEARCH_PREF_MAX_BIT
import com.example.radioplayer.utils.Constants.SEARCH_PREF_MIN_BIT
import com.example.radioplayer.utils.Constants.SEARCH_PREF_NAME
import com.example.radioplayer.utils.Constants.SEARCH_PREF_NAME_AUTO
import com.example.radioplayer.utils.Constants.SEARCH_PREF_ORDER
import com.example.radioplayer.utils.Constants.SEARCH_PREF_TAG
import com.example.radioplayer.utils.Constants.TEXT_SIZE_STATION_TITLE_PREF
import com.example.radioplayer.utils.RandomColors
import com.example.radioplayer.utils.Utils

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val mainViewModel : MainViewModel by viewModels()
    val databaseViewModel : DatabaseViewModel by viewModels()

    lateinit var bind : ActivityMainBinding
    lateinit var bindPlayer : StubPlayerActivityMainBinding
     var isStubPlayerBindInflated = false

    private val separatorAnimation : LoadingAnim by lazy { LoadingAnim(this,
        bind.viewSeparatorStart!!, bind.viewSeparatorEnd!!,
        bind.separatorLowest!!, bind.separatorSecond!!) }

    fun startSeparatorsLoadAnim(){
        separatorAnimation.startLoadingAnim()
    }

    fun endSeparatorsLoadAnim(){
        separatorAnimation.endLoadingAnim()
    }


    private val animationIn : Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.fall_down) }
    val layoutAnimationController : LayoutAnimationController by lazy {
        LayoutAnimationController(animationIn).apply {
            delay = 0.1f
            order = LayoutAnimationController.ORDER_NORMAL
        }
    }



    @Inject
    lateinit var glide : RequestManager

    private var currentPlayingItem : PlayingItem? = null

    companion object{
        var uiMode = 0
        var flHeight = 0

    }



    private val radioSearchFragment : RadioSearchFragment by lazy { RadioSearchFragment() }
    private val favStationsFragment : FavStationsFragment by lazy { FavStationsFragment() }
    private val historyFragment : HistoryFragment by lazy { HistoryFragment() }
    private val recordingsFragment : RecordingsFragment by lazy { RecordingsFragment() }
    private val settingsFragment : SettingsFragment by lazy { SettingsFragment() }


    private val stationDetailsFragment : StationDetailsFragment by lazy { StationDetailsFragment().apply {
        enterTransition = Slide(Gravity.BOTTOM)
        exitTransition = Slide(Gravity.BOTTOM)
        }
    }

    private val recordingDetailsFragment : RecordingDetailsFragment by lazy { RecordingDetailsFragment().apply {
        enterTransition = Slide(Gravity.BOTTOM)
        exitTransition = Slide(Gravity.BOTTOM)
         }
    }


    override fun onBackPressed() {

        if(!isStubPlayerBindInflated) {
            this.moveTaskToBack(true)
        } else if ((bindPlayer.tvExpandHideText as TextView).text  == resources.getString(R.string.Expand)) {
            this.moveTaskToBack(true)
        } else {
            handleNavigationToFragments(null)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_RadioPlayer)

        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.stubPlayer.setOnInflateListener{ _, inflated ->
                bindPlayer = StubPlayerActivityMainBinding.bind(inflated)
        }

        window.navigationBarColor = ContextCompat.getColor(this, R.color.toolbar)

        uiMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        setupInitialNavigation()

        observeNewStation()

        setOnBottomNavClickListener()

        setOnBottomNavItemReselect()

        refreshSeparators()



            bind.root.doOnLayout {
                flHeight = bind.viewHeight.height
            }


    }




    fun smoothDayNightFadeOut(isNightMode : Boolean){

        AlphaFadeOutAnim(1f, 500).apply {

            if(isStubPlayerBindInflated)
                startAnim(bindPlayer.root)

                startAnim(bind.bottomNavigationView)

                bind.viewSeparatorStart?.let{
                    startAnim(bind.viewSeparatorStart!!)
                    startAnim(bind.viewSeparatorEnd!!)
                    startAnim(bind.separatorSecond!!)
                    startAnim(bind.separatorLowest!!)
                }



        }

        if(!isNightMode){

            val colorAnimator = ValueAnimator.ofArgb(Color.WHITE, Color.BLACK)
            colorAnimator.addUpdateListener {
                bind.root.setBackgroundColor(it.animatedValue as Int)
            }
            colorAnimator.duration = 500
            colorAnimator.start()


            val colorFrom = ContextCompat.getColor(this, R.color.nav_bar_settings_frag)

            val barsColorAnimator = ValueAnimator.ofArgb(colorFrom, Color.BLACK)
            barsColorAnimator.addUpdateListener { value ->
                window.navigationBarColor = value.animatedValue as Int
                window.statusBarColor = value.animatedValue as Int
            }
            barsColorAnimator.duration = 500
            barsColorAnimator.start()



        }
    }

    fun smoothDayNightFadeIn(isNightMode: Boolean){
        if(isStubPlayerBindInflated)
            bindPlayer.root.slideAnim(700, 0, R.anim.fade_in_anim)

        bind.bottomNavigationView.slideAnim(700, 0, R.anim.fade_in_anim)
        bind.viewSeparatorStart?.slideAnim(700, 0, R.anim.fade_in_anim)
        bind.viewSeparatorEnd?.slideAnim(700, 0, R.anim.fade_in_anim)
        bind.separatorSecond?.slideAnim(700, 0, R.anim.fade_in_anim)
        bind.separatorLowest?.slideAnim(700, 0, R.anim.fade_in_anim)

        if(!isNightMode){


            val colorAnimator = ValueAnimator.ofArgb(Color.BLACK, Color.WHITE)
            colorAnimator.addUpdateListener {
                bind.rootLayout.setBackgroundColor(it.animatedValue as Int)
            }
            colorAnimator.duration = 700
            colorAnimator.start()

            val colorTo = ContextCompat.getColor(this, R.color.nav_bar_settings_frag)

            val barsColorAnimator = ValueAnimator.ofArgb(Color.BLACK, colorTo)
            barsColorAnimator.addUpdateListener { value ->
                window.navigationBarColor = value.animatedValue as Int
                window.statusBarColor = value.animatedValue as Int
            }
            barsColorAnimator.duration = 700
            barsColorAnimator.start()



        }
    }



    private fun refreshSeparators(){
        if(!mainViewModel.isInitialLaunchOfTheApp && uiMode == Configuration.UI_MODE_NIGHT_YES){
            separatorAnimation.refresh()
        }

    }




    private fun setupInitialNavigation(){

        if(mainViewModel.isInitialLaunchOfTheApp){

            supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, radioSearchFragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun observeNewStation(){

        mainViewModel.newPlayingItem.observe(this){ playingItem ->

            currentPlayingItem = playingItem

            if(!isStubPlayerBindInflated) {
                inflatePlayerStubAndCallRelatedMethods()
            } else if(bindPlayer.root.visibility == View.GONE){

                bindPlayer.root.visibility = View.VISIBLE
                bindPlayer.root.slideAnim(500, 0, R.anim.fade_in_anim)
                bindPlayer.tvStationTitle.isSingleLine = true
                bindPlayer.tvStationTitle.isSelected = true
            }


            updateImage(playingItem)

        }
    }

    private fun inflatePlayerStubAndCallRelatedMethods (){


        isStubPlayerBindInflated = true
        bind.stubPlayer.visibility = View.VISIBLE

        bindPlayer.root.slideAnim(500, 0, R.anim.fade_in_anim)


                bindPlayer.tvStationTitle.isSingleLine = true
                bindPlayer.tvStationTitle.isSelected = true


        clickListenerToHandleNavigationWithDetailsFragment()

        onClickListenerForTogglePlay()
        observePlaybackStateToChangeIcons()

        observeCurrentSongTitle()

    }


    private fun observeCurrentSongTitle (){

        mainViewModel.currentSongTitle.observe(this){ title ->

            handleTitleText(title)
        }
    }


    private fun handleTitleText(title : String){

        if(title.equals("NULL", ignoreCase = true) || title.isBlank()){
            bindPlayer.tvStationTitle.apply {
                text = "Playing: no info"
                setTextColor(ContextCompat.getColor(this@MainActivity,R.color.regular_text_color))
            }
        } else {

            bindPlayer.tvStationTitle.apply {

                text = title
                setTextColor(ContextCompat.getColor(this@MainActivity,R.color.selected_text_color))

            }
        }
    }


    private fun setOnBottomNavClickListener(){

        bind.bottomNavigationView.setOnItemSelectedListener {
           handleNavigationToFragments(it)

        }
    }

    private fun setOnBottomNavItemReselect(){
        bind.bottomNavigationView.setOnItemReselectedListener {

            if(isStubPlayerBindInflated){
                if((bindPlayer.tvExpandHideText as TextView).text == resources.getString(R.string.Hide)){
                    handleNavigationToFragments(it)
                }
            }
        }
    }




    @SuppressLint("ClickableViewAccessibility")
    private fun clickListenerToHandleNavigationWithDetailsFragment(){

        bindPlayer.tvStationTitle.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_DOWN){

                if((bindPlayer.tvExpandHideText as TextView).text == resources.getString(R.string.Expand)) {

                    putFadeOutForDetailsFragment()


                    if(uiMode == Configuration.UI_MODE_NIGHT_YES){
                        endSeparatorsLoadAnim()
                    }


                    supportFragmentManager.beginTransaction().apply {

                        if(mainViewModel.isRadioTrueRecordingFalse){
                            replace(R.id.flFragment, stationDetailsFragment)
                        } else {
                            replace(R.id.flFragment, recordingDetailsFragment)
                        }
                        addToBackStack(null)
                        commit()
                    }

                    bindPlayer.tvExpandHideText.setText(R.string.Hide)

                }

                else {

                    handleNavigationToFragments(null)

                }
            }

            true
        }
    }

    private fun handleNavigationToFragments(item : MenuItem?) : Boolean {

        val menuItem = bind.bottomNavigationView.selectedItemId



        if((item?.itemId ?: menuItem) != R.id.mi_radioSearchFragment){
            if(uiMode == Configuration.UI_MODE_NIGHT_YES){
                endSeparatorsLoadAnim()
            }
        }


        when(item?.itemId ?: menuItem) {
            R.id.mi_radioSearchFragment -> {

                radioSearchFragment.exitTransition = null

                supportFragmentManager.beginTransaction().apply {
                    setCustomAnimations(R.anim.blank_anim, android.R.anim.fade_out)
                    replace(R.id.flFragment, radioSearchFragment)
                    addToBackStack(null)
                    commit()
                }


            }
            R.id.mi_favStationsFragment -> {

                favStationsFragment.exitTransition = null

                supportFragmentManager.beginTransaction().apply {
                    setCustomAnimations(R.anim.blank_anim, android.R.anim.fade_out)
                    replace(R.id.flFragment, favStationsFragment)
                    addToBackStack(null)
                    commit()
                }



            }
            R.id.mi_historyFragment -> {

                historyFragment.exitTransition = null
                supportFragmentManager.beginTransaction().apply {
                    setCustomAnimations(R.anim.blank_anim, android.R.anim.fade_out)
                    replace(R.id.flFragment, historyFragment)
                    addToBackStack(null)
                    commit()
                }



            }

            R.id.mi_recordingsFragment -> {

                recordingsFragment.exitTransition = null
                supportFragmentManager.beginTransaction().apply {
                    setCustomAnimations(R.anim.blank_anim, android.R.anim.fade_out)
                    replace(R.id.flFragment, recordingsFragment)
                    addToBackStack(null)
                    commit()
                }


            }

            R.id.mi_settingsFragment -> {
                settingsFragment.exitTransition = null
                supportFragmentManager.beginTransaction().apply {
                    setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    replace(R.id.flFragment, settingsFragment)
                    addToBackStack(null)
                    commit()
                }




//                val drawable = bindPlayer.root.background as GradientDrawable
//                drawable.mutate()
//                drawable.setStroke(4,color)

            }

        }

        if(isStubPlayerBindInflated){
            (bindPlayer.tvExpandHideText as TextView).setText(R.string.Expand)

        }

        return true
    }


    private fun putFadeOutForDetailsFragment(){

        when(bind.bottomNavigationView.selectedItemId) {
            R.id.mi_radioSearchFragment -> {

                radioSearchFragment.exitTransition = Fade()

            }
            R.id.mi_favStationsFragment -> {

                favStationsFragment.exitTransition = Fade()

            }
            R.id.mi_historyFragment -> {

                historyFragment.exitTransition = Fade()

            }
            R.id.mi_recordingsFragment -> {

                recordingsFragment.exitTransition = Fade()
            }

            R.id.mi_settingsFragment -> {

                settingsFragment.exitTransition = Fade()
            }
        }
    }

    private val randColors = RandomColors()

    private fun setTvPlaceHolderLetter(name : String, isRecording : Boolean){

        val color = randColors.getColor()

        if(isRecording){

            bindPlayer.tvPlaceholder.apply {
                text = "Rec."
                setTextColor(color)
                alpha = 0.6f
                textSize = 28f
            }
        } else {

            var char = 'X'

            for(l in name.indices){
                if(name[l].isLetter()){
                    char = name[l]
                    break
                }
            }

            bindPlayer.tvPlaceholder.apply {
                text = char.toString().uppercase()
                setTextColor(color)
                alpha = 0.6f
                textSize = 40f
            }
        }
    }

    private fun updateImage(playingItem : PlayingItem){

        var newName = ""
        var isRecording = false
        var newImage = ""

        when (playingItem) {
            is PlayingItem.FromRadio -> {
                playingItem.radioStation.apply {
                    newName = name ?: "X"
                    newImage = favicon ?: ""
                    val bits = if(bitrate == 0) "0? kbps" else "$bitrate kbps"
                    bindPlayer.tvBitrate.text = bits

                }
            }
            is PlayingItem.FromRecordings -> {
                isRecording = true
                bindPlayer.tvStationTitle.text = "From recordings"
                bindPlayer.tvBitrate.text = ""
                newName = playingItem.recording.name
                newImage = playingItem.recording.iconUri
            }
        }


            if(newImage.isBlank()){
                bindPlayer.ivCurrentStationImage.visibility = View.GONE
                setTvPlaceHolderLetter(newName, isRecording)

            } else {

                bindPlayer.tvPlaceholder.alpha = 0f
                bindPlayer.ivCurrentStationImage.visibility = View.VISIBLE
                glide
                    .load(newImage)
                    .listener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {

                            bindPlayer.ivCurrentStationImage.visibility = View.GONE
                            setTvPlaceHolderLetter(newName, isRecording)
                            return true
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(bindPlayer.ivCurrentStationImage)
            }
        }


    private fun onClickListenerForTogglePlay(){

        bindPlayer.ivTogglePlayCurrentStation.setOnClickListener {

            currentPlayingItem?.let {

                when(it){
                    is PlayingItem.FromRadio -> {
                     mainViewModel.playOrToggleStation(it.radioStation)

                    }
                    is PlayingItem.FromRecordings -> {

                        mainViewModel.playOrToggleStation(rec = it.recording)
                    }
                }
            }
        }
    }


    private fun observePlaybackStateToChangeIcons (){

        mainViewModel.playbackState.observe(this){

            it?.let {
                    when{
                    it.isPlaying -> {
                        bindPlayer.ivTogglePlayCurrentStation
                            .setImageResource(R.drawable.ic_pause_play)
                        bindPlayer.tvStationTitle.apply {
                            setTextColor(ContextCompat.getColor(this@MainActivity,R.color.selected_text_color))

                        }
                    }
                    it.isPlayEnabled -> {
                        bindPlayer.ivTogglePlayCurrentStation
                            .setImageResource(R.drawable.ic_play_pause)
                        bindPlayer.tvStationTitle.apply {

                            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.regular_text_color))

                        }

                    }
                }
            }
        }
    }





    override fun onStop() {
        super.onStop()
        this.cacheDir.deleteRecursively()
        databaseViewModel.removeUnusedStations()

        mainViewModel.searchPreferences.edit().apply {
            putString(SEARCH_PREF_TAG, mainViewModel.searchParamTag.value)
            putString(SEARCH_PREF_NAME, mainViewModel.searchParamName.value)
            putString(SEARCH_PREF_COUNTRY, mainViewModel.searchParamCountry.value)
            putString(SEARCH_FULL_COUNTRY_NAME, mainViewModel.searchFullCountryName)
            putString(SEARCH_PREF_ORDER, mainViewModel.newSearchOrder)
            putBoolean(IS_NAME_EXACT, mainViewModel.isNameExact)
            putBoolean(IS_TAG_EXACT, mainViewModel.isTagExact)
            putInt(SEARCH_PREF_MIN_BIT, mainViewModel.minBitrateNew)
            putInt(SEARCH_PREF_MAX_BIT, mainViewModel.maxBitrateNew)
            putBoolean(IS_SEARCH_FILTER_LANGUAGE, mainViewModel.isSearchFilterLanguage)
            putBoolean(SEARCH_PREF_NAME_AUTO, mainViewModel.isNameAutoSearch)

        }.apply()

        mainViewModel.textSizePref.edit()
            .putFloat(TEXT_SIZE_STATION_TITLE_PREF, mainViewModel.stationsTitleSize).apply()

        if(mainViewModel.isFabUpdated){
            mainViewModel.fabPref.edit().apply {
                putFloat(FAB_POSITION_X, mainViewModel.fabX)
                putFloat(FAB_POSITION_Y, mainViewModel.fabY)
                putBoolean(IS_FAB_UPDATED, true)
            }.apply()
        }

    }

}

