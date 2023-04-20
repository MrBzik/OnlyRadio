package com.example.radioplayer.ui.fragments


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
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
import com.example.radioplayer.ui.dialogs.HistorySettingsDialog
import com.example.radioplayer.ui.dialogs.RecordingSettingsDialog
import com.example.radioplayer.ui.viewmodels.BluetoothViewModel
import com.example.radioplayer.utils.Constants.BUFFER_PREF
import com.example.radioplayer.utils.Constants.DARK_MODE_PREF
import com.example.radioplayer.utils.Constants.IS_FAB_UPDATED
import com.example.radioplayer.utils.Constants.RECONNECT_PREF
import com.example.radioplayer.utils.Constants.RECORDING_QUALITY_PREF


const val REC_LOWEST = "Very light"
const val REC_LOW = "Light"
const val REC_MEDIUM = "Medium"
const val REC_NORMAL = "Normal"
const val REC_ABOVE_AVERAGE = "Above normal"
const val REC_HIGH = "High"
const val REC_VERY_HIGH = "Very high"
const val REC_SUPER = "Super high"
const val REC_ULTRA = "Ultra high"
const val REC_MAXIMUM = "Maximum"

const val HISTORY_STRING_ONE_DAY = "One day"
const val HISTORY_STRING_3_DATES = "3 dates"
const val HISTORY_STRING_7_DATES = "7 dates"
const val HISTORY_STRING_15_DATES = "15 dates"
const val HISTORY_STRING_21_DATES = "21 dates"
const val HISTORY_STRING_30_DATES = "30 dates"


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



    private val listOfRecOptions : List<String> by lazy { listOf(
        REC_LOWEST, REC_LOW, REC_MEDIUM, REC_NORMAL, REC_ABOVE_AVERAGE, REC_HIGH, REC_VERY_HIGH,
        REC_SUPER, REC_ULTRA, REC_MAXIMUM)
    }

    private val listOfHistoryOptions : List<String> by lazy { listOf(
        HISTORY_STRING_ONE_DAY, HISTORY_STRING_3_DATES, HISTORY_STRING_7_DATES,
        HISTORY_STRING_15_DATES, HISTORY_STRING_21_DATES, HISTORY_STRING_30_DATES)
    }

    private var isNightMode = false

    private val bluetoothViewModel : BluetoothViewModel by lazy{
        ViewModelProvider(requireActivity())[BluetoothViewModel::class.java]
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getInitialUiMode()

        getInitialNightModePref()

        getInitialHistoryOptionValue()

        setReconnectButton()

//        setForegroundPrefButton()

        setSwitchNightModeListener()

        setSwitchNightModePrefListener()

        setupRecSettingClickListener()

        updateRecordingSettingValue()

        historySettingsClickListener()

        setSearchBtnResetListener()

        setPlaybackSpeedButtons()

        setLinkClickListener()

        animateDayNightTransition()

        setToolbar()

//        setBluetoothDialog()

        openAudioSettings()

        setBufferSettingsClickListener()

        setReverbClickListeners()

        setBassBoostClickListeners()

    }

    private fun setBassBoostClickListeners(){

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



    private fun openAudioSettings(){

        bind.btnAudioSettings.setOnClickListener {

            try{
                val intent = Intent(Intent.ACTION_MAIN)
                intent.setClassName("com.sec.android.app.soundalive", "com.sec.android.app.soundalive.SAControlPanelActivity")
                startActivity(intent)
            } catch (e: Exception){

                val intent = Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)
                startActivity(intent)

            }


        }

    }


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

            bind.tvSettingsTitle.visibility = View.GONE

            if(!mainViewModel.isSmoothTransitionNeeded){

            (activity as MainActivity).apply {
                window.navigationBarColor = color
                window.statusBarColor = color
                }
            }

        } else {
            bind.viewToolbar.setBackgroundColor(Color.BLACK)

            bind.tvSettingsTitleDay.visibility = View.GONE
        }
    }


    private fun animateDayNightTransition(){

        if(mainViewModel.isSmoothTransitionNeeded){

            bind.root.slideAnim(700, 0, R.anim.fade_in_anim)

            (activity as MainActivity).smoothDayNightFadeIn(isNightMode)

            mainViewModel.isSmoothTransitionNeeded = false
        }

    }
//
//    private fun setForegroundPrefButton(){
//
//        val foregroundPref = requireContext().getSharedPreferences(FOREGROUND_PREF, Context.MODE_PRIVATE)
//        val initialState = foregroundPref.getBoolean(FOREGROUND_PREF, false)
//
//        bind.switchForegroundPref.apply {
//            isChecked = initialState
//            setOnCheckedChangeListener { _, isChecked ->
//                foregroundPref.edit().putBoolean(FOREGROUND_PREF, isChecked).apply()
//            }
//        }
//    }



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


    private fun setSwitchNightModePrefListener(){

        bind.switchNightModePref.setOnCheckedChangeListener { _, isChecked ->

                darkModePref.edit().putBoolean(DARK_MODE_PREF, isChecked).apply()

        }

    }

    private fun getInitialNightModePref(){

        val isChecked = darkModePref.getBoolean(DARK_MODE_PREF, false)

        bind.switchNightModePref.isChecked = isChecked

    }


    private fun historySettingsClickListener(){

        bind.tvHistorySettingValue.setOnClickListener {

            HistorySettingsDialog(listOfHistoryOptions,
                listOfHistoryOptions.indexOf(bind.tvHistorySettingValue.text),
                requireContext(), databaseViewModel,
            )

            { newOption ->

                val toString = historyOptionToString(newOption)
                bind.tvHistorySettingValue.text = toString

            }.show()
        }
    }


    fun historyOptionToString(option : Int) : String{

        return when(option){
            1 -> HISTORY_STRING_ONE_DAY
            3 -> HISTORY_STRING_3_DATES
            7 -> HISTORY_STRING_7_DATES
            15 -> HISTORY_STRING_15_DATES
            21 -> HISTORY_STRING_21_DATES
            else -> HISTORY_STRING_30_DATES
        }

    }

    private fun getInitialHistoryOptionValue(){

        val option = databaseViewModel.getHistoryOptionsPref()

        val toString = historyOptionToString(option)

        bind.tvHistorySettingValue.text = toString

    }

    private fun updateRecordingSettingValue(){

        val value = recordingQualityPref.getFloat(RECORDING_QUALITY_PREF, 0.4f)

        when(value){
            0.1f -> {
                bind.tvRecordingSettingsValue.text = REC_LOWEST

            }

            0.2f  -> {
                bind.tvRecordingSettingsValue.text = REC_LOW

            }

            0.3f  -> {
                bind.tvRecordingSettingsValue.text = REC_MEDIUM

            }

            0.4f  -> {
                bind.tvRecordingSettingsValue.text = REC_NORMAL

            }

            0.5f  -> {
                bind.tvRecordingSettingsValue.text = REC_ABOVE_AVERAGE

            }

            0.6f  -> {
                bind.tvRecordingSettingsValue.text = REC_HIGH

            }

            0.7f  -> {
                bind.tvRecordingSettingsValue.text = REC_VERY_HIGH

            }

            0.8f  -> {
                bind.tvRecordingSettingsValue.text = REC_SUPER

            }

            0.9f  -> {
                bind.tvRecordingSettingsValue.text = REC_ULTRA

            }

            1f  -> {
                bind.tvRecordingSettingsValue.text = REC_MAXIMUM

            }
        }


    }



    private fun setupRecSettingClickListener(){

        bind.tvRecordingSettingsValue.setOnClickListener {

            RecordingSettingsDialog(
                listOfRecOptions,
                recordingQualityPref,
                requireContext(),
                ) {
                updateRecordingSettingValue()
            }.show()

        }
    }

    private fun setSwitchNightModeListener(){

        bind.switchNightMode.setOnCheckedChangeListener { _, isChecked ->

            mainViewModel.isSmoothTransitionNeeded = true

//           bind.root.slideAnim(500, 0, R.anim.fade_out_anim)

            val fadeAnim = AlphaFadeOutAnim(1f, 500)
            fadeAnim.startAnim(bind.root)


            (activity as MainActivity).smoothDayNightFadeOut(isNightMode)

                bind.root.postDelayed({


                    if(isChecked){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

//                        (activity as MainActivity).bind.root.setBackgroundColor(Color.BLACK)


                    } else{

                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

//                        (activity as MainActivity).bind.root.setBackgroundColor(Color.WHITE)
                    }

                }, 500)

        }
    }

    private fun getInitialUiMode(){

       val mode = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when(mode){
            Configuration.UI_MODE_NIGHT_YES -> {
                bind.switchNightMode.isChecked = true
                isNightMode = true
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                bind.switchNightMode.isChecked = false
                isNightMode = false
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()



        _bind = null

    }

}