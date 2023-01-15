package com.example.radioplayer.ui

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.radioplayer.R
import com.example.radioplayer.data.remote.RadioApi
import com.example.radioplayer.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setupWithNavController(navController)

       bottomNavigationView.setOnItemReselectedListener { /*DO NOTHING*/}




//        CoroutineScope(Dispatchers.IO).launch {
//
//
//        }

//        val exoPlayer = ExoPlayer.Builder(this).build()
//
//        val dataSourceFactory = DefaultDataSource.Factory(this)
//
//        val mediaItem = MediaItem.fromUri(Uri.parse("http://stream.bestfm.sk/128.mp3"))
//
//        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//            .createMediaSource(mediaItem)
//
//        exoPlayer.setMediaSource(audioSource)
//
//        exoPlayer.prepare()

//        val exoPlayerView = findViewById<PlayerView>(R.id.exoPlayer)
//
//        exoPlayerView.player = exoPlayer

//        exoPlayer.playWhenReady = true
    }
}