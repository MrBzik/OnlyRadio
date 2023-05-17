package com.example.radioplayer.ui.fragments


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.radioplayer.R
import com.example.radioplayer.databinding.FragmentSettingsBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.AlphaFadeOutAnim
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.dialogs.BufferSettingsDialog
import com.example.radioplayer.ui.dialogs.HistoryOptionsDialog
import com.example.radioplayer.ui.dialogs.RecordingOptionsDialog
import com.example.radioplayer.ui.dialogs.RecordingSettingsDialog
import com.example.radioplayer.ui.viewmodels.BluetoothViewModel
import com.example.radioplayer.utils.Constants.BUFFER_PREF
import com.example.radioplayer.utils.Constants.DARK_MODE_PREF
import com.example.radioplayer.utils.Constants.FOREGROUND_PREF
import com.example.radioplayer.utils.Constants.HISTORY_PREF
import com.example.radioplayer.utils.Constants.IS_FAB_UPDATED
import com.example.radioplayer.utils.Constants.RECONNECT_PREF
import com.example.radioplayer.utils.Constants.RECORDING_QUALITY_PREF
import com.example.radioplayer.utils.Constants.REC_QUALITY_DEF
import com.example.radioplayer.utils.RecPref



class SettingsFragment : BaseFragment<FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate
) {

    private val recordingQualityPref : SharedPreferences by lazy {
        requireContext().getSharedPreferences(RECORDING_QUALITY_PREF, Context.MODE_PRIVATE)
    }

    private val darkModePref : SharedPreferences by lazy{
        requireContext().getSharedPreferences(DARK_MODE_PREF, Context.MODE_PRIVATE)
    }

    private val bufferPref : SharedPreferences by lazy {
        requireContext().getSharedPreferences(BUFFER_PREF, Context.MODE_PRIVATE)
    }


    private val historyPref : SharedPreferences by lazy {
        requireContext().getSharedPreferences(HISTORY_PREF, Context.MODE_PRIVATE)
    }


    private val bluetoothViewModel : BluetoothViewModel by lazy{
        ViewModelProvider(requireActivity())[BluetoothViewModel::class.java]
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getInitialUiMode()

        setReconnectButton()

        setForegroundPrefButton()

        setSwitchNightModeListener()

        setupRecSettingClickListener()

        historySettingsClickListener()

        setSearchBtnResetListener()

        setPlaybackSpeedButtons()

        setLinkClickListener()

        animateDayNightTransition()

        setToolbar()

//        setBluetoothDialog()


        setBufferSettingsClickListener()

        setReverbClickListeners()

        setVirtualizerClickListeners()

        setAutoSearchByName()

        setStationTitleSize()

    }


    private fun setStationTitleSize(){

        bind.tvStationsTitleSize.apply {

            textSize = mainViewModel.stationsTitleSize

            setOnClickListener {

                val newSize = if(mainViewModel.stationsTitleSize > 14){
                    mainViewModel.stationsTitleSize -2f
                } else {
                    20f
                }

                textSize = newSize
                mainViewModel.stationsTitleSize = newSize
            }
        }
    }


    private fun setAutoSearchByName(){

        bind.switchNameAutoSearchPref.apply {
            isChecked = mainViewModel.isNameAutoSearch

            setOnCheckedChangeListener { _, isChecked ->

                mainViewModel.isNameAutoSearch = isChecked
            }
        }
    }


    private fun setVirtualizerClickListeners(){

        setBassBoostText()

        bind.fabBassBoostLess.setOnClickListener {

            if(RadioService.virtualizerLevel > 0){
                RadioService.virtualizerLevel -= 500
                mainViewModel.changeVirtualizerLevel()
                setBassBoostText()
            }
        }

        bind.fabBassBoostMore.setOnClickListener {

            if(RadioService.virtualizerLevel < 1000){
                RadioService.virtualizerLevel += 500
                mainViewModel.changeVirtualizerLevel()
                setBassBoostText()
            }
        }


    }

    private fun setBassBoostText(){
        bind.tvBassBoostValue.text = when(RadioService.virtualizerLevel){

            0 -> "Range : normal"
            500 -> "Range : broad"
            else -> "Range : widest"

        }



    }


    private fun setReverbClickListeners(){

        setReverbName()

        bind.fabPrevReverb.setOnClickListener {

            if(RadioService.reverbMode == 0){
                RadioService.reverbMode = 6
            } else {
                RadioService.reverbMode -= 1
            }
            setReverbName()

            mainViewModel.changeReverbMode()
        }

        bind.fabNextReverb.setOnClickListener {

            if(RadioService.reverbMode == 6){
                RadioService.reverbMode = 0
            } else {
                RadioService.reverbMode += 1
            }
            setReverbName()

            mainViewModel.changeReverbMode()

        }
    }

    private fun setReverbName(){

       bind.tvReverbValue.text = when(RadioService.reverbMode){
            0 -> "Reverb: none"
            1 -> "Large hall"
            2 -> "Medium hall"
            3 -> "Large room"
            4 -> "Medium room"
            5 -> "Small room"
            else -> "Plate"
        }

    }


    private fun setBufferSettingsClickListener(){


        bind.tvControlBufferValue.setOnClickListener {

            BufferSettingsDialog(requireContext(), bufferPref){ isPlayerToResturt ->

                if(isPlayerToResturt){
                    mainViewModel.restartPlayer()
                }

            }.show()

        }
    }



//    private fun openAudioSettings(){
//
//        bind.btnAudioSettings.setOnClickListener {
//
//            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
//
//            if(intent.resolveActivity(requireContext().packageManager) != null){
//                startActivity(intent)
//            }
//        }
//    }


//
//    private fun changePlayerBtn(){
//
//        bind.switchChangePlayer.setOnCheckedChangeListener { buttonView, isChecked ->
//
//            if(isChecked){
//
//                mainViewModel.changeToGoodPlayer()
//
//            }
//
//            else{
//                mainViewModel.changeToBadPlayer()
//            }
//
//        }
//
//    }

//    private fun setBluetoothDialog(){
//
//        bind.ivCastAudio.setOnClickListener {

//            val bluetoothLauncher = registerForActivityResult(
//                ActivityResultContracts.StartActivityForResult()
//            ){}
//
//            val permissionLauncher = registerForActivityResult(
//                ActivityResultContracts.RequestMultiplePermissions()
//            ){ perms ->
//
//                val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    perms[Manifest.permission.BLUETOOTH_CONNECT] == true
//                } else true
//
//                if(canEnableBluetooth && !isBluetoothEnabled){
//                    bluetoothLauncher.launch(
//                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                    )
//                }
//
//             }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                permissionLauncher.launch(
//                    arrayOf(
//                        Manifest.permission.BLUETOOTH_SCAN,
//                        Manifest.permission.BLUETOOTH_CONNECT
//                    )
//                )
//            }


//                BluetoothDialog(requireContext(), bluetoothViewModel).show()
//
//
//
//
//        }
//
//    }


    private fun setToolbar(){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            bind.viewToolbar.setBackgroundResource(R.drawable.toolbar_settings_vector)

            val color = ContextCompat.getColor(requireContext(), R.color.nav_bar_settings_frag)
//            val colorStatus = ContextCompat.getColor(requireContext(), R.color.status_bar_settings_frag)

          bind.viewSeparator.visibility = View.GONE

            if(!mainViewModel.isSmoothTransitionNeeded){

            (activity as MainActivity).apply {
                window.navigationBarColor = color
                window.statusBarColor = color
                }
            }

        } else {
            bind.viewToolbar.setBackgroundColor(Color.BLACK)
            bind.viewSeparator.visibility = View.VISIBLE

        }
    }


    private fun animateDayNightTransition(){

        if(mainViewModel.isSmoothTransitionNeeded){

            bind.root.slideAnim(700, 0, R.anim.fade_in_anim)

            (activity as MainActivity).smoothDayNightFadeIn()

            mainViewModel.isSmoothTransitionNeeded = false
        }

    }

    private fun setForegroundPrefButton(){

        val foregroundPref : SharedPreferences by lazy{
            requireContext().getSharedPreferences(FOREGROUND_PREF, Context.MODE_PRIVATE)
        }

        bind.switchForegroundPref.apply {
            isChecked = RadioService.isToKillServiceOnAppClose
            setOnCheckedChangeListener { _, isChecked ->
                foregroundPref.edit().putBoolean(FOREGROUND_PREF, isChecked).apply()
                RadioService.isToKillServiceOnAppClose = isChecked
            }
        }
    }



    private fun setReconnectButton(){

        val reconnectPref = requireContext().getSharedPreferences(RECONNECT_PREF, Context.MODE_PRIVATE)

        val initialMode = reconnectPref.getBoolean(RECONNECT_PREF, true)

        bind.switchReconnect.apply {
            isChecked = initialMode

            setOnCheckedChangeListener { _, isChecked ->

                reconnectPref.edit().putBoolean(RECONNECT_PREF, isChecked).apply()
                RadioService.isToReconnect = isChecked
            }
        }
    }

    private fun setPlaybackSpeedButtons(){

        updatePlaybackSpeedDisplayValue()
        updatePlaybackPitchDisplayValue()
        updateLinkIcon()

        bind.fabSpeedMinus.setOnClickListener {

            if(RadioService.playbackSpeedRadio > 10){
                RadioService.playbackSpeedRadio -= 10
                mainViewModel.updateRadioPlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }

        }

        bind.fabSpeedPlus.setOnClickListener {
            if(RadioService.playbackSpeedRadio < 200){
                RadioService.playbackSpeedRadio += 10
                mainViewModel.updateRadioPlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }
        }

        bind.fabPitchMinus.setOnClickListener {

            if(RadioService.playbackPitchRadio > 10){
                RadioService.playbackPitchRadio -= 10
                mainViewModel.updateRadioPlaybackPitch()
                updatePlaybackPitchDisplayValue()
            }
        }

        bind.fabPitchPlus.setOnClickListener {
            if(RadioService.playbackPitchRadio < 200){
                RadioService.playbackPitchRadio += 10
                mainViewModel.updateRadioPlaybackPitch()
                updatePlaybackPitchDisplayValue()
            }
        }
    }


    private fun setLinkClickListener(){
        bind.ivLink.setOnClickListener {
            RadioService.isSpeedPitchLinked = !RadioService.isSpeedPitchLinked
            updateLinkIcon()
        }
    }


    private fun updateLinkIcon(){

        bind.ivLink.apply {
            if(RadioService.isSpeedPitchLinked)
                setImageResource(R.drawable.link)
            else setImageResource(R.drawable.link_off)
        }
    }


    private fun updatePlaybackSpeedDisplayValue(){

        bind.tvPlaybackSpeedTitle.text = "speed : ${RadioService.playbackSpeedRadio}%"
        if(RadioService.isSpeedPitchLinked)
            bind.tvPlaybackPitchTitle.text = "pitch : ${RadioService.playbackSpeedRadio}%"

    }

    private fun updatePlaybackPitchDisplayValue(){

        bind.tvPlaybackPitchTitle.text = "pitch : ${RadioService.playbackPitchRadio}%"
        if(RadioService.isSpeedPitchLinked)
            bind.tvPlaybackSpeedTitle.text = "speed : ${RadioService.playbackPitchRadio}%"

    }

    private fun setSearchBtnResetListener(){

        mainViewModel.apply {
            if(isFabUpdated || isFabMoved){
                bind.tvSearchBtnReset
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.apply_option_text))
            }

        }

        bind.tvSearchBtnReset.setOnClickListener {

            mainViewModel.apply {
                isFabMoved = false
                isFabUpdated = false
                fabPref.edit().putBoolean(IS_FAB_UPDATED, false).apply()
            }

            bind.tvSearchBtnReset
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.selected_station_paused))

        }
    }



    private fun historySettingsClickListener(){

        bind.tvHistorySettingValue.setOnClickListener {

            HistoryOptionsDialog(requireContext(), historyPref, databaseViewModel, mainViewModel).show()

        }
    }





    private fun setupRecSettingClickListener(){

        val initialValue = RecPref.qualityFloatToInt(
            recordingQualityPref.getFloat(RECORDING_QUALITY_PREF, REC_QUALITY_DEF)
        )

        bind.tvRecordingSettingsValue.text = RecPref.setTvRecQualityValue(initialValue)


        bind.tvRecordingSettingsValue.setOnClickListener {

            RecordingOptionsDialog(
                recordingQualityPref,
                requireContext(),
                ) { newValue ->

                bind.tvRecordingSettingsValue.text = RecPref.setTvRecQualityValue(newValue)

            }.show()

        }
    }

    private fun setSwitchNightModeListener(){

        bind.switchNightMode.setOnClickListener {


            mainViewModel.isSmoothTransitionNeeded = true

//           bind.root.slideAnim(500, 0, R.anim.fade_out_anim)

            val fadeAnim = AlphaFadeOutAnim(1f, 500)
            fadeAnim.startAnim(bind.root)


            (activity as MainActivity).smoothDayNightFadeOut()

                bind.root.postDelayed({

                    if(bind.switchNightMode.isChecked){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)


                    } else{

                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                    }

                }, 500)

        }
    }

    private fun getInitialUiMode(){

//       val mode = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when(MainActivity.uiMode){
            Configuration.UI_MODE_NIGHT_YES -> {
                bind.switchNightMode.isChecked = true
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                bind.switchNightMode.isChecked = false
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()



        _bind = null

    }

}