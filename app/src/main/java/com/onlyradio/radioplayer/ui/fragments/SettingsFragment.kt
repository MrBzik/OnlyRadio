package com.onlyradio.radioplayer.ui.fragments


import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.databinding.FragmentSettingsBinding
import com.onlyradio.radioplayer.databinding.StubSettingsExtrasBinding
import com.onlyradio.radioplayer.databinding.StubSettingsGeneralBinding
import com.onlyradio.radioplayer.databinding.StubTvTitleBinding
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.ui.animations.AlphaFadeOutAnim
import com.onlyradio.radioplayer.ui.animations.FADE_IN_DURATION
import com.onlyradio.radioplayer.ui.animations.FADE_OUT_DURATION
import com.onlyradio.radioplayer.ui.animations.SwapTitlesUi
import com.onlyradio.radioplayer.ui.animations.slideAnim
import com.onlyradio.radioplayer.ui.animations.smoothDayNightFadeIn
import com.onlyradio.radioplayer.ui.animations.smoothDayNightFadeOut
import com.onlyradio.radioplayer.ui.dialogs.BufferSettingsDialog
import com.onlyradio.radioplayer.ui.dialogs.HistoryOptionsDialog
import com.onlyradio.radioplayer.ui.dialogs.RecordingOptionsDialog
import com.onlyradio.radioplayer.ui.stubs.GeneralDialogsCall
import com.onlyradio.radioplayer.ui.stubs.SettingsExtras
import com.onlyradio.radioplayer.ui.stubs.SettingsGeneral
import com.onlyradio.radioplayer.ui.viewmodels.BluetoothViewModel
import com.onlyradio.radioplayer.utils.Constants.BUFFER_PREF
import com.onlyradio.radioplayer.utils.Constants.DARK_MODE_PREF
import com.onlyradio.radioplayer.utils.Constants.FRAG_OPTIONS
import com.onlyradio.radioplayer.utils.Constants.HISTORY_PREF
import com.onlyradio.radioplayer.utils.Constants.RECORDING_QUALITY_PREF
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_DEF
import com.onlyradio.radioplayer.utils.RecPref


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
            override fun recOptionsDialog(
//                newValue: (Int) -> Unit
            ) {
                RecordingOptionsDialog(
                    recordingQualityPref,
                    requireContext(),
                ).show()
            }

//            override fun recInitialValue(): Int {
//               return RecPref.qualityFloatToInt(
//                    recordingQualityPref.getFloat(RECORDING_QUALITY_PREF, REC_QUALITY_DEF)
//                )
//            }

            override fun historyDialog() {
                HistoryOptionsDialog(requireContext(), historyPref){
                    historyViewModel.checkAndCleanBookmarkTitles()
                }.show()
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
            (bindTvExtras?.tvTitle as TextView).text = resources.getString(R.string.main_extras_title)
        }

        bind.stubTvGeneral.setOnInflateListener{ _, bindView ->
            bindTvGeneral = StubTvTitleBinding.bind(bindView)
            (bindTvGeneral?.tvTitle as TextView).text = resources.getString(R.string.main_general_title)
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

        setSwitchNightModeListener()

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

            bind.root.slideAnim(FADE_IN_DURATION, 0, R.anim.fade_in_anim)

            (activity as MainActivity).smoothDayNightFadeIn()

            settingsViewModel.isSmoothTransitionNeeded = false
        }

    }


    private fun setSwitchNightModeListener(){

        bindGeneral?.switchNightMode?.setOnClickListener {


            settingsViewModel.isSmoothTransitionNeeded = true

//           bind.root.slideAnim(500, 0, R.anim.fade_out_anim)

            val fadeAnim = AlphaFadeOutAnim(1f, FADE_OUT_DURATION)
            fadeAnim.startAnim(bind.root)


            (activity as MainActivity).smoothDayNightFadeOut()

                bind.root.postDelayed({

                    if(bindGeneral?.switchNightMode?.isChecked == true){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                    } else{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }

                }, FADE_OUT_DURATION)
        }
    }


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