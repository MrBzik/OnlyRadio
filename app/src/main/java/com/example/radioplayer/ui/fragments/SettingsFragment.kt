package com.example.radioplayer.ui.fragments


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.radioplayer.R
import com.example.radioplayer.databinding.FragmentSettingsBinding
import com.example.radioplayer.databinding.StubSettingsExtrasBinding
import com.example.radioplayer.databinding.StubSettingsGeneralBinding
import com.example.radioplayer.databinding.StubTvTitleBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.AlphaFadeOutAnim
import com.example.radioplayer.ui.animations.SwapTitlesUi
import com.example.radioplayer.ui.animations.objectSizeScaleAnimation
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.dialogs.BufferSettingsDialog
import com.example.radioplayer.ui.dialogs.HistoryOptionsDialog
import com.example.radioplayer.ui.dialogs.RecordingOptionsDialog
import com.example.radioplayer.ui.viewmodels.BluetoothViewModel
import com.example.radioplayer.utils.Constants.ADD_RADIO_STATION_URL
import com.example.radioplayer.utils.Constants.BUFFER_PREF
import com.example.radioplayer.utils.Constants.DARK_MODE_PREF
import com.example.radioplayer.utils.Constants.FOREGROUND_PREF
import com.example.radioplayer.utils.Constants.FRAG_OPTIONS
import com.example.radioplayer.utils.Constants.HISTORY_PREF

import com.example.radioplayer.utils.Constants.RECONNECT_PREF
import com.example.radioplayer.utils.Constants.RECORDING_QUALITY_PREF
import com.example.radioplayer.utils.Constants.REC_QUALITY_DEF
import com.example.radioplayer.utils.RecPref
import com.example.radioplayer.utils.TextViewOutlined
import com.example.radioplayer.utils.addImage
import com.example.radioplayer.utils.dpToP
import org.w3c.dom.Text


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


    private lateinit var bindTvGeneral : StubTvTitleBinding
    private lateinit var bindTvExtras : StubTvTitleBinding

    private lateinit var bindGeneral : StubSettingsGeneralBinding
    private lateinit var bindExtras : StubSettingsExtrasBinding

    private var isGeneralLogicSet = false
    private var isExtrasLogicSet = false



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setStubsListeners()

        bind.stubTvExtras.visibility = View.VISIBLE
        bind.stubTvGeneral.visibility = View.VISIBLE


        switchGeneralExtras(false)

        setToolbar()

        animateDayNightTransition()

        setTitleButtonsClickListeners()

//        setBluetoothDialog()


    }


    private fun setStubsListeners(){

        bind.stubTvExtras.setOnInflateListener{ _, bindView ->
            bindTvExtras = StubTvTitleBinding.bind(bindView)
            (bindTvExtras.tvTitle as TextView).text = "Effects"
        }

        bind.stubTvGeneral.setOnInflateListener{ _, bindView ->
            bindTvGeneral = StubTvTitleBinding.bind(bindView)
            (bindTvGeneral.tvTitle as TextView).text = "General"
        }


        bind.stubSettingsGeneral.setOnInflateListener { _, bindView ->
            bindGeneral = StubSettingsGeneralBinding.bind(bindView)
        }

        bind.stubSettingsExtras.setOnInflateListener { _, bindView ->
            bindExtras = StubSettingsExtrasBinding.bind(bindView)
        }
    }



    private fun setExtrasLogic(){

        setPlaybackSpeedButtons()

        setLinkClickListener()

        setReverbClickListeners()

        setVirtualizerClickListeners()
    }

    private fun setGeneralLogic(){

        getInitialUiMode()

        setReconnectButton()

        setForegroundPrefButton()

        setSwitchNightModeListener()

        setupRecSettingClickListener()

        historySettingsClickListener()

//        setSearchBtnResetListener()

        setBufferSettingsClickListener()

        setFullAutoSearch()

        setStationTitleSize()

        setAddStationClickListener()

    }



    private fun setTitleButtonsClickListeners(){

        bindTvGeneral.tvTitle.setOnClickListener {
            if(settingsViewModel.isInSettingsExtras){
                settingsViewModel.isInSettingsExtras = false
                switchGeneralExtras(true)
            }
        }

        bindTvExtras.tvTitle.setOnClickListener {
            if(!settingsViewModel.isInSettingsExtras){
                settingsViewModel.isInSettingsExtras = true
                switchGeneralExtras(true)
            }
        }
    }



    private fun switchGeneralExtras(isToAnimate : Boolean){

        if(settingsViewModel.isInSettingsExtras){

        bind.stubSettingsExtras.visibility = View.VISIBLE
            if(isToAnimate){
                bindExtras.root.slideAnim(350, 100, R.anim.fade_in_anim)
            }

            if(isGeneralLogicSet)
                bind.stubSettingsGeneral.visibility = View.GONE

            if(!isExtrasLogicSet){
                isExtrasLogicSet = true
                setExtrasLogic()
            }

        } else {

            bind.stubSettingsGeneral.visibility = View.VISIBLE
                if(isToAnimate){
                bindGeneral.root.slideAnim(350, 100, R.anim.fade_in_anim)
            }

                if(isExtrasLogicSet)
                    bind.stubSettingsExtras.visibility = View.GONE

                if(!isGeneralLogicSet){
                    isGeneralLogicSet = true
                    setGeneralLogic()
            }
        }

        swapTitlesUi(isToAnimate)

    }

    private fun swapTitlesUi(isToAnimate: Boolean) = SwapTitlesUi.swap(
        conditionA = settingsViewModel.isInSettingsExtras,
        textViewA = bindTvExtras.tvTitle as TextView,
        textViewB = bindTvGeneral.tvTitle as TextView,
        isToAnimate = isToAnimate,
        toolbar = bind.viewToolbar,
        fragment = FRAG_OPTIONS
    )


    private fun setAddStationClickListener(){

        bindGeneral.tvAddStationTitleBtn.setOnClickListener {

            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse(ADD_RADIO_STATION_URL))
            startActivity(intent)
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


//    private fun setAutoSearchByName(){
//
//        bindGeneral.switchNameAutoSearchPref.apply {
//            isChecked = mainViewModel.isNameAutoSearch
//
//            setOnCheckedChangeListener { _, isChecked ->
//
//                mainViewModel.isNameAutoSearch = isChecked
//            }
//        }
//    }

    private fun setFullAutoSearch(){


        bindGeneral.tvFullAutoSearchHint.text =
            "(Default: start new searches manually by Swipe-up or with [icon] button)"

        bindGeneral.tvFullAutoSearchHint.addImage(
            atText = "[icon]",
            imgSrc = R.drawable.ic_new_radio_search,
            imgWidth = 30f.dpToP(requireContext()),
            imgHeight = 30f.dpToP(requireContext())
            )

        bindGeneral.switchFullAutoSearchPref.apply {
            isChecked = mainViewModel.isFullAutoSearch

            setOnCheckedChangeListener { _, isChecked ->

                mainViewModel.isFullAutoSearch = isChecked
            }
        }
    }


    private fun setVirtualizerClickListeners(){

        bindExtras.switchWideRange.apply{
            isChecked = RadioService.isVirtualizerEnabled
            setOnCheckedChangeListener { _, isChecked ->

                RadioService.isVirtualizerEnabled = isChecked
                settingsViewModel.changeVirtualizerLevel()
            }
        }
    }



    private fun setReverbClickListeners(){

        setReverbName()

        bindExtras.fabPrevReverb.setOnClickListener {

            if(RadioService.reverbMode == 0){
                RadioService.reverbMode = 6
            } else {
                RadioService.reverbMode -= 1
            }
            setReverbName()

            settingsViewModel.changeReverbMode()
        }

        bindExtras.fabNextReverb.setOnClickListener {

            if(RadioService.reverbMode == 6){
                RadioService.reverbMode = 0
            } else {
                RadioService.reverbMode += 1
            }
            setReverbName()

            settingsViewModel.changeReverbMode()

        }
    }

    private fun setReverbName(){

        bindExtras.tvReverbValue.text = when(RadioService.reverbMode){
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


        bindGeneral.tvControlBufferValue.setOnClickListener {

            BufferSettingsDialog(requireContext(), bufferPref){ isPlayerToResturt ->

                if(isPlayerToResturt){
                    settingsViewModel.restartPlayer()
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

            val color = ContextCompat.getColor(requireContext(), R.color.nav_bar_settings_frag)
//            val colorStatus = ContextCompat.getColor(requireContext(), R.color.status_bar_settings_frag)

          bind.viewSeparator.visibility = View.GONE

            if(!settingsViewModel.isSmoothTransitionNeeded){

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

        if(settingsViewModel.isSmoothTransitionNeeded){

            bind.root.slideAnim(700, 0, R.anim.fade_in_anim)

            (activity as MainActivity).smoothDayNightFadeIn()

            settingsViewModel.isSmoothTransitionNeeded = false
        }

    }

    private fun setForegroundPrefButton(){

        val foregroundPref : SharedPreferences by lazy{
            requireContext().getSharedPreferences(FOREGROUND_PREF, Context.MODE_PRIVATE)
        }

        bindGeneral.switchForegroundPref.apply {
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

        bindGeneral.switchReconnect.apply {
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

        bindExtras.fabSpeedMinus.setOnClickListener {

            if(RadioService.playbackSpeedRadio > 10){
                RadioService.playbackSpeedRadio -= 10
                settingsViewModel.updateRadioPlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }

        }

        bindExtras.fabSpeedPlus.setOnClickListener {
            if(RadioService.playbackSpeedRadio < 200){
                RadioService.playbackSpeedRadio += 10
                settingsViewModel.updateRadioPlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }
        }

        bindExtras.fabPitchMinus.setOnClickListener {

            if(RadioService.playbackPitchRadio > 10){
                RadioService.playbackPitchRadio -= 10
                settingsViewModel.updateRadioPlaybackPitch()
                updatePlaybackPitchDisplayValue()
            }
        }

        bindExtras.fabPitchPlus.setOnClickListener {
            if(RadioService.playbackPitchRadio < 200){
                RadioService.playbackPitchRadio += 10
                settingsViewModel.updateRadioPlaybackPitch()
                updatePlaybackPitchDisplayValue()
            }
        }
    }


    private fun setLinkClickListener(){
        bindExtras.ivLink.setOnClickListener {
            RadioService.isSpeedPitchLinked = !RadioService.isSpeedPitchLinked
            updateLinkIcon()
        }
    }


    private fun updateLinkIcon(){

        bindExtras.ivLink.apply {
            if(RadioService.isSpeedPitchLinked)
                setImageResource(R.drawable.link)
            else setImageResource(R.drawable.link_off)
        }
    }


    private fun updatePlaybackSpeedDisplayValue(){

        bindExtras.tvPlaybackSpeedValue.text = " ${RadioService.playbackSpeedRadio}%"
        if(RadioService.isSpeedPitchLinked)
            bindExtras.tvPlaybackPitchValue.text = " ${RadioService.playbackSpeedRadio}%"

    }

    private fun updatePlaybackPitchDisplayValue(){

        bindExtras.tvPlaybackPitchValue.text = " ${RadioService.playbackPitchRadio}%"
        if(RadioService.isSpeedPitchLinked)
            bindExtras.tvPlaybackSpeedValue.text = " ${RadioService.playbackPitchRadio}%"

    }

//    private fun setSearchBtnResetListener(){
//
//        mainViewModel.apply {
//            if(isFabUpdated || isFabMoved){
//                bindGeneral.tvSearchBtnReset
//                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.apply_option_text))
//            }
//
//        }
//
//        bindGeneral.tvSearchBtnReset.setOnClickListener {
//
//            mainViewModel.apply {
//                isFabMoved = false
//                isFabUpdated = false
//                fabPref.edit().putBoolean(IS_FAB_UPDATED, false).apply()
//            }
//
//            bindGeneral.tvSearchBtnReset
//                .setTextColor(ContextCompat.getColor(requireContext(), R.color.selected_station_paused))
//
//        }
//    }



    private fun historySettingsClickListener(){

        bindGeneral.tvHistorySettingValue.setOnClickListener {

            HistoryOptionsDialog(requireContext(), historyPref, historyViewModel).show()

        }
    }





    private fun setupRecSettingClickListener(){

        val initialValue = RecPref.qualityFloatToInt(
            recordingQualityPref.getFloat(RECORDING_QUALITY_PREF, REC_QUALITY_DEF)
        )

        bindGeneral.tvRecordingSettingsValue.text = RecPref.setTvRecQualityValue(initialValue)


        bindGeneral.tvRecordingSettingsValue.setOnClickListener {

            RecordingOptionsDialog(
                recordingQualityPref,
                requireContext(),
                ) { newValue ->

                bindGeneral.tvRecordingSettingsValue.text = RecPref.setTvRecQualityValue(newValue)

            }.show()

        }
    }

    private fun setSwitchNightModeListener(){

        bindGeneral.switchNightMode.setOnClickListener {


            settingsViewModel.isSmoothTransitionNeeded = true

//           bind.root.slideAnim(500, 0, R.anim.fade_out_anim)

            val fadeAnim = AlphaFadeOutAnim(1f, 500)
            fadeAnim.startAnim(bind.root)


            (activity as MainActivity).smoothDayNightFadeOut()

                bind.root.postDelayed({

                    if(bindGeneral.switchNightMode.isChecked){
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
                bindGeneral.switchNightMode.isChecked = true
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                bindGeneral.switchNightMode.isChecked = false
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        isGeneralLogicSet = false
        isExtrasLogicSet = false
        _bind = null

    }

}