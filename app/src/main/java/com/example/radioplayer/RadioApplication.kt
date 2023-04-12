package com.example.radioplayer

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaFormat
import android.os.Debug
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.utils.Constants
import dagger.hilt.android.HiltAndroidApp

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

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

    }

}
