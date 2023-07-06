package com.example.radioplayer.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.radioplayer.exoPlayer.RadioServiceConnection
import com.example.radioplayer.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app : Application,
    private val radioServiceConnection: RadioServiceConnection
) : AndroidViewModel(app) {

    var isInSettingsExtras = false
    var isSmoothTransitionNeeded = false

    val textSizePref = app.getSharedPreferences(Constants.TEXT_SIZE_STATION_TITLE_PREF, Context.MODE_PRIVATE)
    var stationsTitleSize = textSizePref.getFloat(Constants.TEXT_SIZE_STATION_TITLE_PREF, 20f)


    fun changeReverbMode(){
        radioServiceConnection.sendCommand(Constants.COMMAND_CHANGE_REVERB_MODE, null)
    }

    fun changeVirtualizerLevel(){
        radioServiceConnection.sendCommand(Constants.COMMAND_TOGGLE_REVERB, null)
    }


    fun restartPlayer(){
        radioServiceConnection.sendCommand(Constants.COMMAND_RESTART_PLAYER, null)
    }

    fun updateRadioPlaybackSpeed(){
        radioServiceConnection.sendCommand(Constants.COMMAND_UPDATE_RADIO_PLAYBACK_SPEED, null)
    }

    fun updateRadioPlaybackPitch(){
        radioServiceConnection.sendCommand(Constants.COMMAND_UPDATE_RADIO_PLAYBACK_PITCH, null)
    }


}