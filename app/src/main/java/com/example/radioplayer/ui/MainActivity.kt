package com.example.radioplayer.ui

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.radioplayer.R
import com.example.radioplayer.connectivityObserver.ConnectivityObserver
import com.example.radioplayer.connectivityObserver.NetworkConnectivityObserver
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.ActivityMainBinding
import com.example.radioplayer.databinding.StubPlayerActivityMainBinding
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.fragments.*
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.Constants.FAB_POSITION_X
import com.example.radioplayer.utils.Constants.FAB_POSITION_Y
import com.example.radioplayer.utils.Constants.IS_FAB_UPDATED
import com.example.radioplayer.utils.Constants.SEARCH_PREF_COUNTRY
import com.example.radioplayer.utils.Constants.SEARCH_PREF_NAME
import com.example.radioplayer.utils.Constants.SEARCH_PREF_TAG

import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val mainViewModel : MainViewModel by viewModels()
    val databaseViewModel : DatabaseViewModel by viewModels()


    lateinit var bind : ActivityMainBinding


    lateinit var bindPlayer : StubPlayerActivityMainBinding

    private var isStubPlayerBindInflated = false

    @Inject
    lateinit var glide : RequestManager

    private var currentStation : RadioStation? = null
    private var isFavoured = false

    private  val radioSearchFragment : RadioSearchFragment by lazy { RadioSearchFragment() }
    private  val favStationsFragment : FavStationsFragment by lazy { FavStationsFragment() }
    private  val historyFragment : HistoryFragment by lazy { HistoryFragment() }
    private  val stationDetailsFragment : StationDetailsFragment by lazy { StationDetailsFragment() }
    private var previousImageUri : Uri? = null

    override fun onBackPressed() {

        if(!isStubPlayerBindInflated) {
            this.moveTaskToBack(true)
        } else if (bindPlayer.tvExpandHideText.text == resources.getString(R.string.Expand)) {
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

        setupInitialNavigation()

        observeInternetConnection()

        observeNewStation()

        setOnBottomNavClickListener()

        setOnBottomNavItemReselect()

        observeInternetConnection()


    }

    private fun observeInternetConnection() {

        mainViewModel.hasInternetConnection.observe(this){
            bind.ivNoInternet.isVisible = !it
        }
    }

    private fun setupInitialNavigation(){


        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, radioSearchFragment)
            addToBackStack(null)
            commit()
        }

    }

    private fun observeNewStation(){

        mainViewModel.newRadioStation.observe(this){ station ->

            currentStation = station

            if(!isStubPlayerBindInflated) {
                inflatePlayerStubAndCallRelatedMethods()
            }

            checkIfStationFavoured(station)

            updateImageAndTitle(station)

        }
    }

    private fun inflatePlayerStubAndCallRelatedMethods (){


        isStubPlayerBindInflated = true
        bind.stubPlayer.visibility = View.VISIBLE

        bindPlayer.root.slideAnim(500, 0, R.anim.fade_in_anim)

        clickListenerToHandleNavigationWithDetailsFragment()
        observeIfNewStationFavoured()
        addToFavClickListener()
        onClickListenerForTogglePlay()
        observePlaybackStateToChangeIcons()
    }



    private fun setOnBottomNavClickListener(){

        bind.bottomNavigationView.setOnItemSelectedListener {
           handleNavigationToFragments(it)

        }
    }

    private fun setOnBottomNavItemReselect(){
        bind.bottomNavigationView.setOnItemReselectedListener {

            if(isStubPlayerBindInflated){
                if(bindPlayer.tvExpandHideText.text == resources.getString(R.string.Hide)){
                    handleNavigationToFragments(it)
                }
            }
        }
    }

    private fun observeIfNewStationFavoured(){

        databaseViewModel.isStationFavoured.observe(this){

            paintButtonAddToFav(it)

            isFavoured = it


        }
    }



    private fun paintButtonAddToFav(isInDB : Boolean){
        if(!isInDB){
            bind.fabAddToFav.setImageResource(R.drawable.ic_add_to_fav)

        } else {
            bind.fabAddToFav.setImageResource(R.drawable.ic_added_to_fav)

        }
    }



    private fun clickListenerToHandleNavigationWithDetailsFragment(){

        bindPlayer.tvStationTitle.setOnClickListener{

            if(bindPlayer.tvExpandHideText.text == resources.getString(R.string.Expand)) {

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, stationDetailsFragment)
                    addToBackStack(null)
                    commit()
                }


                bindPlayer.tvExpandHideText.setText(R.string.Hide)
                bind.fabAddToFav.isVisible = true
          
            }

            else {
                handleNavigationToFragments(null)
            }
        }

    }

    private fun handleNavigationToFragments(item : MenuItem?) : Boolean {

        val menuItem = bind.bottomNavigationView.selectedItemId

        when(item?.itemId ?: menuItem) {
            R.id.mi_radioSearchFragment -> {

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, radioSearchFragment)
                    addToBackStack(null)
                    commit()
                }
            }
            R.id.mi_favStationsFragment -> {

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, favStationsFragment)
                    addToBackStack(null)
                    commit()
                }
            }
            R.id.mi_historyFragment -> {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, historyFragment)
                    addToBackStack(null)
                    commit()
                }
            }
        }

        if(isStubPlayerBindInflated){
            bindPlayer.tvExpandHideText.setText(R.string.Expand)
            bind.fabAddToFav.visibility = View.GONE
        }

        return true
    }

    private fun addToFavClickListener(){

        bind.fabAddToFav.setOnClickListener {

            if(isFavoured) {

                currentStation?.let {
                    databaseViewModel.updateIsFavouredState(0, it.stationuuid)
                    Snackbar.make(findViewById(R.id.rootLayout),
                        "Station removed from favs", Snackbar.LENGTH_SHORT).show()
                    databaseViewModel.isStationFavoured.postValue(false)
                }

            } else {
                currentStation?.let {
                    databaseViewModel.updateIsFavouredState(System.currentTimeMillis(), it.stationuuid)
                    Snackbar.make(findViewById(R.id.rootLayout),
                        "Station saved to favs", Snackbar.LENGTH_SHORT).show()
                    databaseViewModel.isStationFavoured.postValue(true)
                }
            }
        }
    }





    private fun checkIfStationFavoured(station: RadioStation){
        databaseViewModel.checkIfStationIsFavoured(station.stationuuid)
    }


    private fun updateImageAndTitle(station : RadioStation){

        val newImage = station.favicon?.toUri()

        newImage?.let { uri ->

            if(previousImageUri == uri){/*DO NOTHING*/} else{

                glide
                    .load(uri)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(bindPlayer.ivCurrentStationImage)
                previousImageUri = uri
            }
        }

        bindPlayer.tvStationTitle.text = station.name

    }

    private fun onClickListenerForTogglePlay(){

        bindPlayer.ivTogglePlayCurrentStation.setOnClickListener {

            currentStation?.let {

                mainViewModel.playOrToggleStation(it)
            }
        }
    }


    private fun observePlaybackStateToChangeIcons (){

        mainViewModel.playbackState.observe(this){

            it?.let {
                when{
                    it.isPlaying -> bindPlayer.ivTogglePlayCurrentStation.setImageResource(R.drawable.ic_pause_play)
                    it.isPlayEnabled -> bindPlayer.ivTogglePlayCurrentStation.setImageResource(R.drawable.ic_play_pause)
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
        }.apply()

        if(mainViewModel.isFabUpdated){
            mainViewModel.fabPref.edit().apply {
                putFloat(FAB_POSITION_X, mainViewModel.fabX)
                putFloat(FAB_POSITION_Y, mainViewModel.fabY)
                putBoolean(IS_FAB_UPDATED, true)
            }.apply()
        }

    }

}

