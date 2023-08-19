package com.onlyradio.radioplayer.ui.stubs

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.databinding.StubSettingsGeneralBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.ui.viewmodels.MainViewModel
import com.onlyradio.radioplayer.ui.viewmodels.SettingsViewModel
import com.onlyradio.radioplayer.utils.Constants
import com.onlyradio.radioplayer.utils.addImage
import com.onlyradio.radioplayer.utils.dpToP

class SettingsGeneral () {


    private lateinit var bindGeneral : StubSettingsGeneralBinding
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var mainViewModel: MainViewModel

    private val context by lazy {
        bindGeneral.root.context
    }

    private lateinit var generalDialogsCall : GeneralDialogsCall


    fun setFields(
        settingsModel: SettingsViewModel,
        mainModel: MainViewModel,
        dialogsCall: GeneralDialogsCall
    ){
        settingsViewModel = settingsModel
        mainViewModel = mainModel
        generalDialogsCall = dialogsCall
    }

    fun updateBinding(bind : StubSettingsGeneralBinding){
        bindGeneral = bind
    }


    fun setGeneralLogic(){

        getInitialUiMode()

        setReconnectButton()

        setForegroundPrefButton()

        setupRecSettingClickListener()

        historySettingsClickListener()

        setBufferSettingsClickListener()

        setFullAutoSearch()

        setStationTitleSize()

        setAddStationClickListener()

    }


    private fun getInitialUiMode(){

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

    private fun setReconnectButton(){

        val reconnectPref = context.getSharedPreferences(Constants.RECONNECT_PREF, Context.MODE_PRIVATE)

        val initialMode = reconnectPref.getBoolean(Constants.RECONNECT_PREF, true)

        bindGeneral.switchReconnect.apply {
            isChecked = initialMode

            setOnCheckedChangeListener { _, isChecked ->

                reconnectPref.edit().putBoolean(Constants.RECONNECT_PREF, isChecked).apply()
                RadioService.isToReconnect = isChecked
            }
        }
    }

    private fun setForegroundPrefButton(){

        val foregroundPref : SharedPreferences by lazy{
            context.getSharedPreferences(Constants.FOREGROUND_PREF, Context.MODE_PRIVATE)
        }

        bindGeneral.switchForegroundPref.apply {
            isChecked = RadioService.isToKillServiceOnAppClose
            setOnCheckedChangeListener { _, isChecked ->
                foregroundPref.edit().putBoolean(Constants.FOREGROUND_PREF, isChecked).apply()
                RadioService.isToKillServiceOnAppClose = isChecked
            }
        }
    }


    private fun setupRecSettingClickListener(){

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

    private fun historySettingsClickListener(){

        bindGeneral.tvHistorySettingValue.setOnClickListener {

            generalDialogsCall.historyDialog()

        }
    }


    private fun setBufferSettingsClickListener(){

        bindGeneral.tvControlBufferValue.setOnClickListener {

           generalDialogsCall.bufferDialog()

        }
    }


    private fun setFullAutoSearch(){


        bindGeneral.tvFullAutoSearchHint.text =
            bindGeneral.root.context.getString(R.string.auto_search_hint)

        bindGeneral.tvFullAutoSearchHint.addImage(
            atText = "[icon]",
            imgSrc = R.drawable.ic_new_radio_search,
            imgWidth = 30f.dpToP(context),
            imgHeight = 30f.dpToP(context)
        )

        bindGeneral.switchFullAutoSearchPref.apply {
            isChecked = mainViewModel.isFullAutoSearch

            setOnCheckedChangeListener { _, isChecked ->

                mainViewModel.isFullAutoSearch = isChecked
            }
        }
    }

    private fun setStationTitleSize(){

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

    private fun setAddStationClickListener(){

        bindGeneral.tvAddStationTitleBtn.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(Constants.ADD_RADIO_STATION_URL))
            context.startActivity(intent)
        }
    }

}