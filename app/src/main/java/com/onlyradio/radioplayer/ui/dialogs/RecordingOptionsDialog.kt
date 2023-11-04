package com.onlyradio.radioplayer.ui.dialogs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.material.slider.RangeSlider
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.databinding.DialogRecordingOptionsBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.utils.Constants.RECORDING_AUTO_STOP_PREF
import com.onlyradio.radioplayer.utils.Constants.RECORDING_NAMING_PREF
import com.onlyradio.radioplayer.utils.Constants.RECORDING_QUALITY_PREF
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_DEF
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_HIGH
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_LOW
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_MAX
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_MEDIUM
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_ULTRA


class RecordingOptionsDialog (
    private val recordingQualityPref : SharedPreferences,
    private val requireContext : Context,
//    private val updateTvValue : (Int) -> Unit
    )
    : BaseDialog<DialogRecordingOptionsBinding>
    (requireContext, DialogRecordingOptionsBinding::inflate) {

    private var newQualityOption : Int? = null
    private var newAutoStopOption : Int? = null
    private var newNamingPref : Boolean? = null

    private var timeMax = ""
    private var timeHours = ""
    private var timeMins = ""
    private var timeMinLong = ""

    private var initialRecQuality =
        when (recordingQualityPref.getFloat(RECORDING_QUALITY_PREF, REC_QUALITY_DEF)) {
        REC_QUALITY_LOW -> 0
        REC_QUALITY_MEDIUM -> 1
        REC_QUALITY_DEF -> 2
        REC_QUALITY_HIGH -> 3
        REC_QUALITY_ULTRA -> 4
        else -> 5
    }

    private var initialRecAutoStopMins = recordingQualityPref.getInt(RECORDING_AUTO_STOP_PREF, 180)

    private var initialNamingPref = recordingQualityPref.getBoolean(RECORDING_NAMING_PREF, false)

    private val recQualityArray = requireContext.resources.getStringArray(R.array.rec_quality_array)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTimeStrings()

        setRangeRecQuality()

        setRangeAutoStop()

        setNamingSwitchListener()

        bind.tvBack.setOnClickListener {
            dismiss()
        }

        bind.tvAccept.setOnClickListener {

            newQualityOption?.let { option ->

                if(option != initialRecQuality){

//                updateTvValue(option)

                recordingQualityPref.edit().putFloat(RECORDING_QUALITY_PREF,
                    qualityIntToFloat(option)
                    ).apply()

                }
            }

            newAutoStopOption?.let { option ->

                if(option != initialRecAutoStopMins){

                    recordingQualityPref.edit().putInt(RECORDING_AUTO_STOP_PREF,
                    option).apply()

                    RadioService.autoStopRec = option
                }
            }


            newNamingPref?.let { option ->

                if(option != initialNamingPref){
                    recordingQualityPref.edit().putBoolean(
                        RECORDING_NAMING_PREF, option).apply()
                    RadioService.isToUseTitleForRecNaming = option
                }
            }

            dismiss()
        }

    }


    private fun setTimeStrings(){
        timeMins = requireContext.resources.getString(R.string.time_mins)
        timeHours = requireContext.resources.getString(R.string.time_hours)
        timeMax = requireContext.resources.getString(R.string.setting_unlimited)
        timeMinLong = requireContext.getString(R.string.rec_setting_min)
    }

    private fun setRangeRecQuality(){
        bind.rangeSliderRecQuality.apply {
            valueFrom = 0f
            valueTo = 5f
            stepSize = 1f


//            bind.tvRecQualityValue.text = RecPref.setTvRecQualityValue(initialRecQuality)

            bind.tvQualityEstimate.text = setTvEstimateText(initialRecQuality)

            values = listOf(initialRecQuality.toFloat())

            setLabelFormatter { value ->
                recQualityArray[value.toInt()]
            }


            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
                override fun onStartTrackingTouch(slider: RangeSlider) {

                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    newQualityOption = slider.values.first().toInt()

                    newQualityOption?.let { value ->
//                        bind.tvRecQualityValue.text = RecPref.setTvRecQualityValue(value)
                        bind.tvQualityEstimate.text = setTvEstimateText(value)
                    }
                }
            })
        }
    }



    private fun setTvEstimateText(value : Int) : String  {

      return when(value){
            0 -> "1 $timeMinLong ~ 500kb"
            1 -> "1 $timeMinLong ~ 750kb"
            2 -> "1 $timeMinLong ~ 1mb"
            3 -> "1 $timeMinLong ~ 1.5mb"
            4 -> "1 $timeMinLong ~ 2mb"
            else -> "1 $timeMinLong ~ 2.3mb"
        }
    }


   private fun qualityIntToFloat(value : Int) : Float {

        return when(value){
            0 -> REC_QUALITY_LOW
            1 -> REC_QUALITY_MEDIUM
            2 -> REC_QUALITY_DEF
            3 -> REC_QUALITY_HIGH
            4 -> REC_QUALITY_ULTRA
            else -> REC_QUALITY_MAX
        }
    }


    private fun setRangeAutoStop(){
        bind.rangeSliderAutoStop.apply {
            valueFrom = 5f
            valueTo = 180f
            stepSize = 5f

            bind.tvRecAutoStopValue.text = minsToString(initialRecAutoStopMins)

            values = listOf(initialRecAutoStopMins.toFloat())

            setLabelFormatter { value ->
                minsToString(value.toInt())
            }


            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
                override fun onStartTrackingTouch(slider: RangeSlider) {

                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    newAutoStopOption = slider.values.first().toInt()

                    newAutoStopOption?.let { value ->
                        bind.tvRecAutoStopValue.text = minsToString(value)
                    }
                }
            })
        }
    }


    private fun minsToString(mins : Int) : String{

        if(mins == 180)
            return timeMax

        val hours = mins / 60

        val minsRemain = mins -( hours * 60)

        return if (hours > 0){
            "$hours $timeHours $minsRemain $timeMins"
        } else {
            "$minsRemain $timeMins"
        }
    }

    private fun setNamingSwitchListener(){

        bind.switchNaming.isChecked = initialNamingPref

        bind.switchNaming.setOnCheckedChangeListener { buttonView, isChecked ->
            newNamingPref = isChecked
        }
    }


    override fun onStop() {
        super.onStop()
        _bind = null
    }

}