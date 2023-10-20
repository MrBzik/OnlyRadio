package com.onlyradio.radioplayer.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LOGGER
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installStatus
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.onlyradio.radioplayer.exoPlayer.RadioServiceConnection
import com.onlyradio.radioplayer.utils.Commands.COMMAND_CHANGE_REVERB_MODE
import com.onlyradio.radioplayer.utils.Commands.COMMAND_RESTART_PLAYER
import com.onlyradio.radioplayer.utils.Commands.COMMAND_TOGGLE_REVERB
import com.onlyradio.radioplayer.utils.Commands.COMMAND_UPDATE_RADIO_PLAYBACK_PITCH
import com.onlyradio.radioplayer.utils.Commands.COMMAND_UPDATE_RADIO_PLAYBACK_SPEED
import com.onlyradio.radioplayer.utils.Constants
import com.onlyradio.radioplayer.utils.Constants.TEXT_SIZE_DEFAULT
import com.onlyradio.radioplayer.utils.Constants.UPDATES_AUTO_PREF
import com.onlyradio.radioplayer.utils.Constants.UPDATES_AVAILABLE
import com.onlyradio.radioplayer.utils.Constants.UPDATES_DOWNLOADED
import com.onlyradio.radioplayer.utils.Constants.UPDATES_DOWNLOADING
import com.onlyradio.radioplayer.utils.Constants.UPDATES_FAILED
import com.onlyradio.radioplayer.utils.Constants.UPDATES_INSTALLING
import com.onlyradio.radioplayer.utils.Constants.UPDATES_NOT_AVAILABLE
import com.onlyradio.radioplayer.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app : Application,
    private val radioServiceConnection: RadioServiceConnection
) : AndroidViewModel(app) {

    var isInSettingsExtras = false
    var isSmoothTransitionNeeded = false

    val textSizePref = app.getSharedPreferences(Constants.TEXT_SIZE_STATION_TITLE_PREF, Context.MODE_PRIVATE)
    var stationsTitleSize = textSizePref.getFloat(Constants.TEXT_SIZE_STATION_TITLE_PREF, TEXT_SIZE_DEFAULT)


    private val updatesPref = app.getSharedPreferences(Constants.UPDATES_PREF, Context.MODE_PRIVATE)

    private val _updatesStatus = MutableStateFlow(UPDATES_NOT_AVAILABLE)
    val updatesStatus = _updatesStatus.asStateFlow()


    lateinit var updateInfo : AppUpdateInfo

    private val _updatesInitialize = Channel<Boolean>()

    val updatesInitialize = _updatesInitialize.receiveAsFlow()

    fun onUpdatesSuccessListener(info: AppUpdateInfo){
        updateInfo = info

        val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        val isUpdateAllowed = info.isFlexibleUpdateAllowed

        _updatesStatus.value =
            if(isUpdateAvailable && isUpdateAllowed) UPDATES_AVAILABLE
        else UPDATES_NOT_AVAILABLE

        if(checkUpdatesPref())
            requestUpdates(false)
    }

    fun requestUpdates(isFromUser : Boolean){

        if (_updatesStatus.value == UPDATES_AVAILABLE){
            viewModelScope.launch {
                _updatesInitialize.send(isFromUser)
            }
        }
    }

    fun changeAutoUpdatesPref(isChecked: Boolean){
        updatesPref.edit().putBoolean(UPDATES_AUTO_PREF, isChecked).apply()
    }

    fun onInstallUpdateStatus(state: InstallState){

        _updatesStatus.value =
            when(state.installStatus()){

            InstallStatus.DOWNLOADED -> {
                UPDATES_DOWNLOADED
            }

            InstallStatus.DOWNLOADING -> {
                UPDATES_DOWNLOADING
            }

            InstallStatus.FAILED -> {
                UPDATES_FAILED
            }

            InstallStatus.INSTALLING -> {
                UPDATES_INSTALLING
            }

                else -> -1
            }
    }

    fun checkUpdatesPref() : Boolean {
        return updatesPref.getBoolean(UPDATES_AUTO_PREF, false)
    }

    fun changeReverbMode(){
        radioServiceConnection.sendCommand(COMMAND_CHANGE_REVERB_MODE, null)
    }

    fun changeVirtualizerLevel(){
        radioServiceConnection.sendCommand(COMMAND_TOGGLE_REVERB, null)
    }


    fun restartPlayer(){
        radioServiceConnection.sendCommand(COMMAND_RESTART_PLAYER, null)
    }

    fun updateRadioPlaybackSpeed(){
        radioServiceConnection.sendCommand(COMMAND_UPDATE_RADIO_PLAYBACK_SPEED, null)
    }

    fun updateRadioPlaybackPitch(){
        radioServiceConnection.sendCommand(COMMAND_UPDATE_RADIO_PLAYBACK_PITCH, null)
    }

}