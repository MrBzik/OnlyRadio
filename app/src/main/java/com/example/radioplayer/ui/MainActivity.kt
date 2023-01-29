package com.example.radioplayer.ui

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.Constants.MAIN_PLAY_LIST
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val mainViewModel : MainViewModel by viewModels()
    val databaseViewModel : DatabaseViewModel by viewModels()

    @Inject
    lateinit var glide : RequestManager
    lateinit var currStationImage : ImageView
    lateinit var currStationTitle : TextView
    lateinit var togglePlay : ImageView
    lateinit var tvExpandHide : TextView
    lateinit var fabAddToFav : FloatingActionButton

    private val colorGray = Color.DKGRAY
    private val colorRed = Color.RED
    private var currentStation : RadioStation? = null
    private var isFavoured = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.navigationBarColor = colorGray

        currStationImage = findViewById(R.id.ivCurrentSongImage)
        currStationTitle = findViewById(R.id.tvStationTitle)
        togglePlay = findViewById(R.id.ivTogglePlayCurrentSong)
        tvExpandHide = findViewById(R.id.tvExpandHideText)
        fabAddToFav = findViewById(R.id.fabAddToFav)


        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemReselectedListener {/*DO NOTHING*/}

        clickListenerToHandleNavigationWithDetailsFragment(navController, bottomNavigationView)

        listenerToHandleNavFromDetailsFragmentToOtherFragments(navController)

        observeNewStation()

        observeIfNewStationExistsInDB()

        observeIfNewStationFavoured()

        onClickListenerForTogglePlay()

        observePlaybackStateToChangeIcons()

        addToFavClickListener()

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
            fabAddToFav.backgroundTintList = ColorStateList.valueOf(colorGray)

        } else {
            fabAddToFav.backgroundTintList = ColorStateList.valueOf(colorRed)

        }
    }


    private fun listenerToHandleNavFromDetailsFragmentToOtherFragments(
        navController: NavController
    ){
        navController.addOnDestinationChangedListener{ _, destination, _ ->

            if(tvExpandHide.text == "HIDE") {
                tvExpandHide.setText(R.string.Expand)

            }

            fabAddToFav.visibility = View.GONE

        }
    }

    private fun clickListenerToHandleNavigationWithDetailsFragment(
        navController: NavController,
        bottomNavigationView: BottomNavigationView
    ){

        currStationTitle.setOnClickListener{

            if(tvExpandHide.text == "EXPAND") {
                navController.navigate(R.id.expandToStationDetails)
                tvExpandHide.setText(R.string.Hide)
                fabAddToFav.isVisible = true

            }

            else {
                val item = bottomNavigationView.selectedItemId

                if(item == R.id.radioSearchFragment) {
                    navController.navigate(R.id.action_stationDetailsFragment_to_radioSearchFragment2)
                } else {
                    navController.navigate(R.id.action_DetailsFrag_to_Fav_frag)
                }

                tvExpandHide.setText(R.string.Expand)
                fabAddToFav.visibility = View.GONE
            }
        }


    }

    private fun addToFavClickListener(){

        fabAddToFav.setOnClickListener {

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
            glide.load(uri).into(currStationImage)
        } ?: run {
            currStationImage.setImageResource(R.drawable.ic_radio_default)
        }

        currStationTitle.text = station.name

    }

    private fun onClickListenerForTogglePlay(){

        togglePlay.setOnClickListener {

            currentStation?.let {

                mainViewModel.playOrToggleStation(it, true)
            }
        }
    }


    private fun observePlaybackStateToChangeIcons (){

        mainViewModel.playbackState.observe(this){

            it?.let {
                when{
                    it.isPlaying -> togglePlay.setImageResource(R.drawable.ic_pause_play)
                    it.isPlayEnabled -> togglePlay.setImageResource(R.drawable.ic_play_pause)
                }
            }
        }

    }

}

