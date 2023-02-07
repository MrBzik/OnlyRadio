package com.example.radioplayer.ui

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.ActivityMainBinding
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.fragments.FavStationsFragment
import com.example.radioplayer.ui.fragments.HistoryFragment
import com.example.radioplayer.ui.fragments.RadioSearchFragment
import com.example.radioplayer.ui.fragments.StationDetailsFragment
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


    private val colorGray = Color.DKGRAY
    private val colorRed = Color.RED
    private var currentStation : RadioStation? = null
    private var isFavoured = false

    private lateinit var radioSearchFragment : RadioSearchFragment
    private lateinit var favStationsFragment : FavStationsFragment
    private lateinit var historyFragment : HistoryFragment
    private lateinit var stationDetailsFragment : StationDetailsFragment

    override fun onBackPressed() {

        if(bind.tvExpandHideText.text == "EXPAND") {
            this.moveTaskToBack(true)
        } else {
            handleNavigationToFragments(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)


        window.navigationBarColor = colorGray


        setupInitialNavigation()

        clickListenerToHandleNavigationWithDetailsFragment()

        observeNewStation()

        observeIfNewStationExistsInDB()

        observeIfNewStationFavoured()

        onClickListenerForTogglePlay()

        observePlaybackStateToChangeIcons()

        addToFavClickListener()

        setOnBottomNavClickListener()

        setOnBottomNavItemReselect()



    }

    private fun setupInitialNavigation(){

        radioSearchFragment = RadioSearchFragment()
        favStationsFragment = FavStationsFragment()
        historyFragment = HistoryFragment()
        stationDetailsFragment = StationDetailsFragment()


        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, radioSearchFragment)
            addToBackStack(null)
            commit()
        }

    }


    private fun setOnBottomNavClickListener(){

        bind.bottomNavigationView.setOnItemSelectedListener {
           handleNavigationToFragments(it)

        }
    }

    private fun setOnBottomNavItemReselect(){
        bind.bottomNavigationView.setOnItemReselectedListener {
            if(bind.tvExpandHideText.text == "HIDE"){
                handleNavigationToFragments(it)
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



    private fun clickListenerToHandleNavigationWithDetailsFragment(){

        bind.tvStationTitle.setOnClickListener{

            if(bind.tvExpandHideText.text == "EXPAND") {

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, stationDetailsFragment)
                    addToBackStack(null)
                    commit()
                }


                bind.tvExpandHideText.setText(R.string.Hide)
                bind.fabAddToFav.isVisible = true

            }

            else {
                handleNavigationToFragments(null)
            }
        }

    }

    private fun handleNavigationToFragments(item : MenuItem?) : Boolean {

        val menuItem = bind.bottomNavigationView.selectedItemId

        bind.tvExpandHideText.setText(R.string.Expand)
        bind.fabAddToFav.visibility = View.GONE

        when(item?.itemId ?: menuItem) {
            R.id.mi_radioSearchFragment -> {

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, radioSearchFragment)
                    addToBackStack(null)
                    commit()
                    if(bind.tvExpandHideText.text == "HIDE") {
                        bind.tvExpandHideText.setText(R.string.Expand)

                    }

                    bind.fabAddToFav.visibility = View.GONE

                }

                return true
            }
            R.id.mi_favStationsFragment -> {

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, favStationsFragment)
                    addToBackStack(null)
                    commit()
                    if(bind.tvExpandHideText.text == "HIDE") {
                        bind.tvExpandHideText.setText(R.string.Expand)

                    }

                    bind.fabAddToFav.visibility = View.GONE
                }

                return true
            }
            R.id.mi_historyFragment -> {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, historyFragment)
                    addToBackStack(null)
                    commit()
                    if(bind.tvExpandHideText.text == "HIDE") {
                        bind.tvExpandHideText.setText(R.string.Expand)

                    }

                    bind.fabAddToFav.visibility = View.GONE
                }
                return true
            }
            else -> return false
        }
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

