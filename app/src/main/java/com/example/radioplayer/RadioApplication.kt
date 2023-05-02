package com.example.radioplayer

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaFormat
import android.os.Debug
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.utils.Constants
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltAndroidApp
class RadioApplication : Application() {



    override fun onCreate() {
        super.onCreate()


        handleDarkMode()

    }


    private fun handleDarkMode(){

       val darkModePref =
            this.getSharedPreferences(Constants.DARK_MODE_PREF, Context.MODE_PRIVATE)

        val isDarkMode = darkModePref.getBoolean(Constants.DARK_MODE_PREF, false)

        if(isDarkMode){

            setDefaultNightMode(MODE_NIGHT_YES)
        }

    }

}
