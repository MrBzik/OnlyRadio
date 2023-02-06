package com.example.radioplayer.ui

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.ActivityMainBinding
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val mainViewModel : MainViewModel by viewModels()
    val databaseViewModel : DatabaseViewModel by viewModels()

    lateinit var bind : ActivityMainBinding

    @Inject
    lateinit var glide : RequestManager

    lateinit var navController: NavController

    private val colorGray = Color.DKGRAY
    private val colorRed = Color.RED
    private var currentStation : RadioStation? = null
    private var isFavoured = false


    override fun onBackPressed() {

        if(bind.tvExpandHideText.text == "EXPAND") {
            this.moveTaskToBack(true)
        } else {
            handleNavigationToFragments()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)


        window.navigationBarColor = colorGray

        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.navHostFragment) as NavHostFragment
         navController = navHostFragment.navController



        clickListenerToHandleNavigationWithDetailsFragment()

        destinationListenerToHandleDetailsUI()

        observeNewStation()

        observeIfNewStationExistsInDB()

        observeIfNewStationFavoured()

        onClickListenerForTogglePlay()

        observePlaybackStateToChangeIcons()

        addToFavClickListener()

        setOnBottomNavClickListener()

        setOnBottomNavItemReselect()

    }


    private fun setOnBottomNavClickListener(){

        bind.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.mi_radioSearchFragment -> {
                    navController.navigate(R.id.action_navigate_to_search_frag)
                    true
                }
                R.id.mi_favStationsFragment -> {
                    navController.navigate(R.id.action_navigate_to_fav_frag)
                    true
                }
                R.id.mi_historyFragment -> {
                    navController.navigate(R.id.action_navigate_to_history_frag)
                    true
                }
                else -> false
            }
        }
    }

    private fun setOnBottomNavItemReselect(){
        bind.bottomNavigationView.setOnItemReselectedListener {
            if(bind.tvExpandHideText.text == "HIDE"){
                handleNavigationToFragments()
            }
        }
    }

    private fun observeIfNewStationExistsInDB(){

        databaseViewModel.isStationInDB.observe(this){

            addNewStationToHistory(it)

        }

    }

    private fun observeIfNewStationFavoured(){

        databaseViewModel.isStationFavoured.observe(this){

            paintButtonAddToFav(it)

            isFavoured = it


        }
    }

    private fun addNewStationToHistory(isInDB: Boolean){

        if(isInDB) {/*DO NOTHING*/}
        else {
            currentStation?.let {
                databaseViewModel.insertRadioStation(it)
            }

        }

    }


    private fun paintButtonAddToFav(isInDB : Boolean){
        if(!isInDB){
            bind.fabAddToFav.backgroundTintList = ColorStateList.valueOf(colorGray)

        } else {
            bind.fabAddToFav.backgroundTintList = ColorStateList.valueOf(colorRed)

        }
    }


    private fun destinationListenerToHandleDetailsUI(){
        navController.addOnDestinationChangedListener{ _, destination, _ ->

            if(bind.tvExpandHideText.text == "HIDE") {
                bind.tvExpandHideText.setText(R.string.Expand)

            }

            bind.fabAddToFav.visibility = View.GONE

        }
    }

    private fun clickListenerToHandleNavigationWithDetailsFragment(){

        bind.tvStationTitle.setOnClickListener{

            if(bind.tvExpandHideText.text == "EXPAND") {
                navController.navigate(R.id.expandToStationDetails)
                bind.tvExpandHideText.setText(R.string.Hide)
                bind.fabAddToFav.isVisible = true

            }

            else {
                handleNavigationToFragments()
            }
        }

    }

    private fun handleNavigationToFragments(){
        val item = bind.bottomNavigationView.selectedItemId

        if(item == R.id.mi_radioSearchFragment) {
            navController.navigate(R.id.action_navigate_to_search_frag)
        }
        else if (item == R.id.mi_favStationsFragment) {
            navController.navigate(R.id.action_navigate_to_fav_frag)
        } else {
            navController.navigate(R.id.action_navigate_to_history_frag)
        }

        bind.tvExpandHideText.setText(R.string.Expand)
        bind.fabAddToFav.visibility = View.GONE
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
                    databaseViewModel.updateIsFavouredState(1, it.stationuuid)
                    Snackbar.make(findViewById(R.id.rootLayout),
                        "Station saved to favs", Snackbar.LENGTH_SHORT).show()
                    databaseViewModel.isStationFavoured.postValue(true)
                }
            }
        }
    }


    private fun observeNewStation(){

        mainViewModel.newRadioStation.observe(this){ station ->

            currentStation = station

            checkIfStationFavoured(station)

            updateImageAndTitle(station)

        }
    }

    private fun checkIfStationFavoured(station: RadioStation){
        databaseViewModel.checkIfStationIsFavoured(station.stationuuid)
    }


    private fun updateImageAndTitle(station : RadioStation){

        val newImage = station.favicon?.toUri()

        newImage?.let { uri ->
            glide.load(uri).into(bind.ivCurrentStationImage)
        } ?: run {
            bind.ivCurrentStationImage.setImageResource(R.drawable.ic_radio_default)
        }

        bind.tvStationTitle.text = station.name

    }

    private fun onClickListenerForTogglePlay(){

        bind.ivTogglePlayCurrentStation.setOnClickListener {

            currentStation?.let {

                mainViewModel.playOrToggleStation(it)
            }
        }
    }


    private fun observePlaybackStateToChangeIcons (){

        mainViewModel.playbackState.observe(this){

            it?.let {
                when{
                    it.isPlaying -> bind.ivTogglePlayCurrentStation.setImageResource(R.drawable.ic_pause_play)
                    it.isPlayEnabled -> bind.ivTogglePlayCurrentStation.setImageResource(R.drawable.ic_play_pause)
                }
            }
        }

    }

}

