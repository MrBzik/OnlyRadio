package com.onlyradio.radioplayer.ui.stubs

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.view.View
import androidx.core.view.isVisible
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.databinding.StubSettingsGeneralBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.extensions.makeToast
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.ui.viewmodels.MainViewModel
import com.onlyradio.radioplayer.ui.viewmodels.SettingsViewModel
import com.onlyradio.radioplayer.utils.Constants
import com.onlyradio.radioplayer.utils.UpdatesStatus
import com.onlyradio.radioplayer.utils.addImage
import com.onlyradio.radioplayer.utils.dpToP

class SettingsGeneral {


    fun setGeneralLogic(
        bindGeneral: StubSettingsGeneralBinding,
        generalDialogsCall: GeneralDialogsCall,
        mainViewModel: MainViewModel,
        settingsViewModel: SettingsViewModel
    ){

        getInitialUiMode(bindGeneral)

        setReconnectButton(bindGeneral)

        setForegroundPrefButton(bindGeneral)

        setDialogCallers(bindGeneral = bindGeneral, generalDialogsCall = generalDialogsCall)

        setFullAutoSearch(bindGeneral, mainViewModel)

        setStationTitleSize(bindGeneral, settingsViewModel)

        setAddStationClickListener(bindGeneral)

        setUpdatesSwitchListener(bindGeneral, settingsViewModel)

        setCheckForUpdatesClickListener(bindGeneral, settingsViewModel)

    }


    private fun setCheckForUpdatesClickListener(bindGeneral : StubSettingsGeneralBinding, settingsViewModel: SettingsViewModel){

        bindGeneral.tvUpdatesAvailableCheck.setOnClickListener {
            settingsViewModel.requestUpdates(true)
        }
    }


    private fun setUpdatesSwitchListener(bindGeneral : StubSettingsGeneralBinding, settingsViewModel: SettingsViewModel){

        bindGeneral.switchUpdatesAutoCheck.isChecked = settingsViewModel.checkUpdatesPref()

        bindGeneral.tvUpdatesAvailableCheck.isVisible = !settingsViewModel.checkUpdatesPref()

        bindGeneral.switchUpdatesAutoCheck.setOnCheckedChangeListener { _, isChecked ->

            settingsViewModel.changeAutoUpdatesPref(isChecked)
            if(isChecked)
                bindGeneral.tvUpdatesAvailableCheck.visibility = View.GONE
            else bindGeneral.tvUpdatesAvailableCheck.visibility = View.VISIBLE

        }
    }


    fun onUpdateStatus(status : UpdatesStatus, bindGeneral : StubSettingsGeneralBinding) {

        val context = bindGeneral.root.context

        val text = when(status){
            UpdatesStatus.UPDATES_AVAILABLE ->
                context.getString(R.string.updates_status_available)
            UpdatesStatus.UPDATES_NOT_AVAILABLE ->
                context.getString(R.string.updates_status_not_available)
            UpdatesStatus.UPDATES_DOWNLOADING ->
                context.getString(R.string.updates_status_downloading)
            UpdatesStatus.UPDATES_DOWNLOADED ->
                context.getString(R.string.updates_status_downloaded)
            UpdatesStatus.UPDATES_INSTALLING ->
                context.getString(R.string.updates_status_installing)
            UpdatesStatus.UPDATES_FAILED ->
                context.getString(R.string.updates_status_failed)
            UpdatesStatus.UPDATES_REQUESTED ->
                context.getString(R.string.updates_status_requested)
            UpdatesStatus.UPDATES_PENDING ->
                context.getString(R.string.updates_status_pending)
        }

        bindGeneral.tvUpdatesAvailableCheck.text = text
    }



    private fun getInitialUiMode(bindGeneral : StubSettingsGeneralBinding){

//       val mode = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when(MainActivity.uiMode){
            Configuration.UI_MODE_NIGHT_YES -> {
                bindGeneral.switchNightMode.isChecked = true
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                bindGeneral.switchNightMode.isChecked = false
            }
        }
    }

    private fun setReconnectButton(bindGeneral : StubSettingsGeneralBinding){

        val reconnectPref = bindGeneral.root.context.getSharedPreferences(Constants.RECONNECT_PREF, Context.MODE_PRIVATE)

        val initialMode = reconnectPref.getBoolean(Constants.RECONNECT_PREF, true)

        bindGeneral.switchReconnect.apply {
            isChecked = initialMode

            setOnCheckedChangeListener { _, isChecked ->

                reconnectPref.edit().putBoolean(Constants.RECONNECT_PREF, isChecked).apply()
                RadioService.isToReconnect = isChecked
            }
        }
    }

    private fun setForegroundPrefButton(bindGeneral : StubSettingsGeneralBinding){

        val foregroundPref : SharedPreferences by lazy{
            bindGeneral.root.context.getSharedPreferences(Constants.FOREGROUND_PREF, Context.MODE_PRIVATE)
        }

        bindGeneral.switchForegroundPref.apply {
            isChecked = RadioService.isToKillServiceOnAppClose
            setOnCheckedChangeListener { _, isChecked ->
                foregroundPref.edit().putBoolean(Constants.FOREGROUND_PREF, isChecked).apply()
                RadioService.isToKillServiceOnAppClose = isChecked
            }
        }
    }



    private fun setDialogCallers(generalDialogsCall: GeneralDialogsCall, bindGeneral : StubSettingsGeneralBinding){

         fun setupRecSettingClickListener(){

//        val initialValue = generalDialogsCall.recInitialValue()

//        bindGeneral.tvRecordingSettingsValue.text = RecPref.setTvRecQualityValue(initialValue)


            bindGeneral.tvRecordingSettingsValue.setOnClickListener {

                generalDialogsCall.recOptionsDialog()
//           {
//                   newValue ->
//               bindGeneral.tvRecordingSettingsValue.text = RecPref.setTvRecQualityValue(newValue)
//           }
            }
        }

         fun historySettingsClickListener(){

            bindGeneral.tvHistorySettingValue.setOnClickListener {

                generalDialogsCall.historyDialog()

            }
        }


         fun setBufferSettingsClickListener(){

            bindGeneral.tvControlBufferValue.setOnClickListener {

                generalDialogsCall.bufferDialog()

            }
        }

        setupRecSettingClickListener()

        historySettingsClickListener()

        setBufferSettingsClickListener()


    }





    private fun setFullAutoSearch(bindGeneral : StubSettingsGeneralBinding, mainViewModel: MainViewModel){


        bindGeneral.tvFullAutoSearchHint.text =
            bindGeneral.root.context.getString(R.string.auto_search_hint)

        bindGeneral.tvFullAutoSearchHint.addImage(
            atText = "[icon]",
            imgSrc = R.drawable.ic_new_radio_search,
            imgWidth = 30f.dpToP(bindGeneral.root.context),
            imgHeight = 30f.dpToP(bindGeneral.root.context)
        )

        bindGeneral.switchFullAutoSearchPref.apply {
            isChecked = mainViewModel.isFullAutoSearch

            setOnCheckedChangeListener { _, isChecked ->

                mainViewModel.isFullAutoSearch = isChecked
            }
        }
    }

    private fun setStationTitleSize(bindGeneral : StubSettingsGeneralBinding, settingsViewModel : SettingsViewModel){

        bindGeneral.tvStationsTitleSize.apply {

            textSize = settingsViewModel.stationsTitleSize

            setOnClickListener {

                val newSize = if(settingsViewModel.stationsTitleSize > 14){
                    settingsViewModel.stationsTitleSize -2f
                } else {
                    20f
                }

                textSize = newSize
                settingsViewModel.stationsTitleSize = newSize
            }
        }
    }

    private fun setAddStationClickListener(bindGeneral : StubSettingsGeneralBinding){

        bindGeneral.tvAddStationTitleBtn.setOnClickListener {

            val context = bindGeneral.root.context

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(Constants.ADD_RADIO_STATION_URL))
            try {
                context.startActivity(intent)
            } catch (e : Exception){
                context.makeToast(R.string.no_browser_error)
            }
        }
    }

}