package com.example.radioplayer.ui.fragments


import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.example.radioplayer.databinding.FragmentSettingsBinding
import com.example.radioplayer.ui.dialogs.HistorySettingsDialog
import com.example.radioplayer.ui.dialogs.RecordingSettingsDialog
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
const val HISTORY_STRING_30_DATES = "30 dates"
const val HISTORY_STRING_NEVER_CLEAN = "Never clean"



class SettingsFragment : BaseFragment<FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate
) {

    private val recordingQualityPref : SharedPreferences by lazy {
        requireContext().getSharedPreferences(RECORDING_QUALITY_PREF, Context.MODE_PRIVATE)
    }

    private val listOfRecOptions : List<String> by lazy { listOf(
        REC_LOWEST, REC_LOW, REC_MEDIUM, REC_NORMAL, REC_ABOVE_AVERAGE, REC_HIGH, REC_VERY_HIGH,
        REC_SUPER, REC_ULTRA, REC_MAXIMUM)
    }

    private val listOfHistoryOptions : List<String> by lazy { listOf(
        HISTORY_STRING_ONE_DAY, HISTORY_STRING_3_DATES, HISTORY_STRING_7_DATES,
        HISTORY_STRING_15_DATES, HISTORY_STRING_30_DATES, HISTORY_STRING_NEVER_CLEAN)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getInitialUiMode()

        getInitialHistoryOptionValue()

        setSwitchNightModeListener()

        setupRecSettingClickListener()

        updateRecordingSettingValue()

        historySettingsClickListener()

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
            30 -> HISTORY_STRING_30_DATES
            else -> HISTORY_STRING_NEVER_CLEAN
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

            if(isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            } else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            }
        }
    }

    private fun getInitialUiMode(){

       val mode = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when(mode){
            Configuration.UI_MODE_NIGHT_YES -> bind.switchNightMode.isChecked = true
            Configuration.UI_MODE_NIGHT_NO -> bind.switchNightMode.isChecked = false
        }
    }

}