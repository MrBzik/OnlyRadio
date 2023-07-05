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
import com.example.radioplayer.ui.stubs.GeneralDialogsCall
import com.example.radioplayer.ui.stubs.SettingsExtras
import com.example.radioplayer.ui.stubs.SettingsGeneral
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


    private var bindTvGeneral : StubTvTitleBinding? = null
    private var bindTvExtras : StubTvTitleBinding? = null

    private var bindGeneral : StubSettingsGeneralBinding? = null
    private var bindExtras : StubSettingsExtrasBinding? = null

    private var isGeneralLogicSet = false
    private var isExtrasLogicSet = false

    private val settingsExtras by lazy {
        SettingsExtras().apply {
            setFields(settingsViewModel)
        }
    }

    private val settingsGeneral by lazy {
        SettingsGeneral().apply {
            setFields(
                settingsViewModel, mainViewModel, generalDialogsCall
            )
        }
    }

    private val generalDialogsCall by lazy {
        object : GeneralDialogsCall{
            override fun recOptionsDialog(newValue: (Int) -> Unit) {
                RecordingOptionsDialog(
                    recordingQualityPref,
                    requireContext(),
                    newValue).show()
            }

            override fun recInitialValue(): Int {
               return RecPref.qualityFloatToInt(
                    recordingQualityPref.getFloat(RECORDING_QUALITY_PREF, REC_QUALITY_DEF)
                )
            }

            override fun historyDialog() {
                HistoryOptionsDialog(requireContext(), historyPref, historyViewModel).show()
            }

            override fun bufferDialog() {
                BufferSettingsDialog(requireContext(), bufferPref){ isPlayerToRestart ->

                    if(isPlayerToRestart){
                        settingsViewModel.restartPlayer()
                    }
                }.show()
            }
        }
    }


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
            (bindTvExtras?.tvTitle as TextView).text = "Effects"
        }

        bind.stubTvGeneral.setOnInflateListener{ _, bindView ->
            bindTvGeneral = StubTvTitleBinding.bind(bindView)
            (bindTvGeneral?.tvTitle as TextView).text = "General"
        }


        bind.stubSettingsGeneral.setOnInflateListener { _, bindView ->
            bindGeneral = StubSettingsGeneralBinding.bind(bindView)
            bindGeneral?.let {
                settingsGeneral.updateBinding(it)
            }
        }

        bind.stubSettingsExtras.setOnInflateListener { _, bindView ->
            bindExtras = StubSettingsExtrasBinding.bind(bindView)
            bindExtras?.let {
                settingsExtras.updateBinding(it)
            }
        }
    }



    private fun setExtrasLogic(){
        settingsExtras.setExtrasLogic()
    }

    private fun setGeneralLogic(){

        settingsGeneral.setGeneralLogic()


//        getInitialUiMode()
//
//        setReconnectButton()
//
//        setForegroundPrefButton()

        setSwitchNightModeListener()

//        setupRecSettingClickListener()
//
//        historySettingsClickListener()
//
//        setBufferSettingsClickListener()
//
//        setFullAutoSearch()
//
//        setStationTitleSize()
//
//        setAddStationClickListener()

    }



    private fun setTitleButtonsClickListeners(){

        bindTvGeneral?.tvTitle?.setOnClickListener {
            if(settingsViewModel.isInSettingsExtras){
                settingsViewModel.isInSettingsExtras = false
                switchGeneralExtras(true)
            }
        }

        bindTvExtras?.tvTitle?.setOnClickListener {
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
                bindExtras?.root?.slideAnim(350, 100, R.anim.fade_in_anim)
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
                bindGeneral?.root?.slideAnim(350, 100, R.anim.fade_in_anim)
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
        textViewA = bindTvExtras?.tvTitle as TextView,
        textViewB = bindTvGeneral?.tvTitle as TextView,
        isToAnimate = isToAnimate,
        toolbar = bind.viewToolbar,
        fragment = FRAG_OPTIONS
    )


//    private fun setAddStationClickListener(){
//
//        bindGeneral.tvAddStationTitleBtn.setOnClickListener {
//
//            val intent = Intent(Intent.ACTION_VIEW,
//                Uri.parse(ADD_RADIO_STATION_URL))
//            startActivity(intent)
//        }
//    }


//    private fun setStationTitleSize(){
//
//        bindGeneral.tvStationsTitleSize.apply {
//
//            textSize = settingsViewModel.stationsTitleSize
//
//            setOnClickListener {
//
//                val newSize = if(settingsViewModel.stationsTitleSize > 14){
//                    settingsViewModel.stationsTitleSize -2f
//                } else {
//                    20f
//                }
//
//                textSize = newSize
//                settingsViewModel.stationsTitleSize = newSize
//            }
//        }
//    }


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

//    private fun setFullAutoSearch(){
//
//
//        bindGeneral.tvFullAutoSearchHint.text =
//            "(Default: start new searches manually by Swipe-up or with [icon] button)"
//
//        bindGeneral.tvFullAutoSearchHint.addImage(
//            atText = "[icon]",
//            imgSrc = R.drawable.ic_new_radio_search,
//            imgWidth = 30f.dpToP(requireContext()),
//            imgHeight = 30f.dpToP(requireContext())
//            )
//
//        bindGeneral.switchFullAutoSearchPref.apply {
//            isChecked = mainViewModel.isFullAutoSearch
//
//            setOnCheckedChangeListener { _, isChecked ->
//
//                mainViewModel.isFullAutoSearch = isChecked
//            }
//        }
//    }







//    private fun setBufferSettingsClickListener(){
//
//
//        bindGeneral.tvControlBufferValue.setOnClickListener {
//
//            BufferSettingsDialog(requireContext(), bufferPref){ isPlayerToResturt ->
//
//                if(isPlayerToResturt){
//                    settingsViewModel.restartPlayer()
//                }
//            }.show()
//        }
//    }



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

//    private fun setForegroundPrefButton(){
//
//        val foregroundPref : SharedPreferences by lazy{
//            requireContext().getSharedPreferences(FOREGROUND_PREF, Context.MODE_PRIVATE)
//        }
//
//        bindGeneral.switchForegroundPref.apply {
//            isChecked = RadioService.isToKillServiceOnAppClose
//            setOnCheckedChangeListener { _, isChecked ->
//                foregroundPref.edit().putBoolean(FOREGROUND_PREF, isChecked).apply()
//                RadioService.isToKillServiceOnAppClose = isChecked
//            }
//        }
//    }



//    private fun setReconnectButton(){
//
//        val reconnectPref = requireContext().getSharedPreferences(RECONNECT_PREF, Context.MODE_PRIVATE)
//
//        val initialMode = reconnectPref.getBoolean(RECONNECT_PREF, true)
//
//        bindGeneral.switchReconnect.apply {
//            isChecked = initialMode
//
//            setOnCheckedChangeListener { _, isChecked ->
//
//                reconnectPref.edit().putBoolean(RECONNECT_PREF, isChecked).apply()
//                RadioService.isToReconnect = isChecked
//            }
//        }
//    }




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



//    private fun historySettingsClickListener(){
//
//        bindGeneral.tvHistorySettingValue.setOnClickListener {
//
//            HistoryOptionsDialog(requireContext(), historyPref, historyViewModel).show()
//
//        }
//    }





//    private fun setupRecSettingClickListener(){
//
//        val initialValue = RecPref.qualityFloatToInt(
//            recordingQualityPref.getFloat(RECORDING_QUALITY_PREF, REC_QUALITY_DEF)
//        )
//
//        bindGeneral.tvRecordingSettingsValue.text = RecPref.setTvRecQualityValue(initialValue)
//
//
//        bindGeneral.tvRecordingSettingsValue.setOnClickListener {
//
//            RecordingOptionsDialog(
//                recordingQualityPref,
//                requireContext(),
//                ) { newValue ->
//
//                bindGeneral.tvRecordingSettingsValue.text = RecPref.setTvRecQualityValue(newValue)
//
//            }.show()
//
//        }
//    }

    private fun setSwitchNightModeListener(){

        bindGeneral?.switchNightMode?.setOnClickListener {


            settingsViewModel.isSmoothTransitionNeeded = true

//           bind.root.slideAnim(500, 0, R.anim.fade_out_anim)

            val fadeAnim = AlphaFadeOutAnim(1f, 500)
            fadeAnim.startAnim(bind.root)


            (activity as MainActivity).smoothDayNightFadeOut()

                bind.root.postDelayed({

                    if(bindGeneral?.switchNightMode?.isChecked == true){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)


                    } else{

                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                    }

                }, 500)
        }
    }

//    private fun getInitialUiMode(){
//
//        when(MainActivity.uiMode){
//            Configuration.UI_MODE_NIGHT_YES -> {
//                bindGeneral.switchNightMode.isChecked = true
//            }
//
//            Configuration.UI_MODE_NIGHT_NO -> {
//                bindGeneral.switchNightMode.isChecked = false
//            }
//        }
//    }


    override fun onDestroyView() {
        super.onDestroyView()
        isGeneralLogicSet = false
        isExtrasLogicSet = false
        _bind = null
        bindTvGeneral = null
        bindTvExtras = null
        bindExtras = null
        bindGeneral = null

    }

}