package com.example.radioplayer.ui

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.exoPlayer.toRadioStation
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

   private val colorGray = Color.GRAY
   private val colorRed = Color.RED


    private var currentStation : RadioStation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        bottomNavigationView.setOnItemReselectedListener { /*DO NOTHING*/}


        databaseViewModel.isExisting.observe(this){

            if(!it){
                fabAddToFav.backgroundTintList = ColorStateList.valueOf(colorGray)


            } else {
               fabAddToFav.backgroundTintList = ColorStateList.valueOf(colorRed)

            }

        }

        currStationTitle.setOnClickListener{

            if(tvExpandHide.text == "EXPAND") {
                navController.navigate(R.id.expandToStationDetails)
                tvExpandHide.setText(R.string.Hide)
                fabAddToFav.isVisible = true
            }

            else {
                navController.popBackStack()
                tvExpandHide.setText(R.string.Expand)
                fabAddToFav.visibility = View.GONE
            }
        }


        fabAddToFav.setOnClickListener {

           if(databaseViewModel.isExisting.value == false) {
               databaseViewModel.insertRadioStation(currentStation!!)
               Toast.makeText(this, "Station added", Toast.LENGTH_SHORT).show()
               databaseViewModel.isExisting.postValue(true)

           } else {

               Toast.makeText(this, "Station already in the db", Toast.LENGTH_SHORT).show()
           }
        }


        mainViewModel.currentRadioStation.observe(this){

            currentStation = it?.toRadioStation()

            currentStation?.let { station ->

                databaseViewModel.ifAlreadyInDatabase(station.stationuuid)
            }

           val newImage = it?.description?.iconUri

            newImage?.let { uri ->
                glide.load(uri).into(currStationImage)
            } ?: run {
                currStationImage.setImageResource(R.drawable.ic_radio_default)
            }

            val newTitle = it?.description?.title!!
            currStationTitle.text = newTitle

        }



        togglePlay.setOnClickListener {
            mainViewModel.togglePlayFromMain()
        }

        mainViewModel.playbackState.observe(this){

          it?.let {
              when{
                  it.isPlaying -> togglePlay.setImageResource(R.drawable.ic_pause_play)
                  it.isPlayEnabled -> togglePlay.setImageResource(R.drawable.ic_play_pause)
              }
          }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        hideSystemUI()
//    }
//
//
//    private fun hideSystemUI() {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        WindowInsetsControllerCompat(window,
//            window.decorView.findViewById(android.R.id.content)).let { controller ->
//            controller.hide(WindowInsetsCompat.Type.systemBars())
//
//            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }
//    }



}

