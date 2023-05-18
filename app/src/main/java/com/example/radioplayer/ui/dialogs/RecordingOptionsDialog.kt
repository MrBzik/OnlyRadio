package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle

import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter

import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.SelectingOptionAdapter
import com.example.radioplayer.databinding.DialogRecordingOptionsBinding

import com.example.radioplayer.databinding.DialogRecordingSettingsBinding

import com.example.radioplayer.utils.Constants.RECORDING_QUALITY_PREF
import com.example.radioplayer.utils.Constants.REC_QUALITY_DEF
import com.example.radioplayer.utils.Constants.REC_QUALITY_HIGH
import com.example.radioplayer.utils.Constants.REC_QUALITY_LOW
import com.example.radioplayer.utils.Constants.REC_QUALITY_MAX
import com.example.radioplayer.utils.Constants.REC_QUALITY_MEDIUM
import com.example.radioplayer.utils.Constants.REC_QUALITY_ULTRA
import com.example.radioplayer.utils.RecPref
import com.google.android.material.slider.RangeSlider


class RecordingOptionsDialog (
    private val recordingQualityPref : SharedPreferences,
    private val requireContext : Context,
    private val updateTvValue : (Int) -> Unit
    )
    : BaseDialog<DialogRecordingOptionsBinding>
    (requireContext, DialogRecordingOptionsBinding::inflate) {

    private var newOption : Int? = null

    private var initialRecQuality = RecPref.qualityFloatToInt(
        recordingQualityPref.getFloat(RECORDING_QUALITY_PREF, REC_QUALITY_DEF)
    )




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setRangeRecQuality()

        bind.tvBack.setOnClickListener {
            dismiss()
        }

        bind.tvAccept.setOnClickListener {

            newOption?.let { option ->

                if(option != initialRecQuality){

                updateTvValue(option)

                recordingQualityPref.edit().putFloat(RECORDING_QUALITY_PREF,
                    qualityIntToFloat(option)
                    ).apply()

                }
            }
            dismiss()
        }



    }



    private fun setRangeRecQuality(){
        bind.rangeSliderRecQuality.apply {
            valueFrom = 1f
            valueTo = 6f
            stepSize = 1f


            bind.tvRecQualityValue.text = RecPref.setTvRecQualityValue(initialRecQuality)

            bind.tvQualityEstimate.text = setTvEstimateText(initialRecQuality)

            values = listOf(initialRecQuality.toFloat())

            setLabelFormatter { value ->
                RecPref.setTvRecQualityValue(value.toInt())
            }


            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
                override fun onStartTrackingTouch(slider: RangeSlider) {

                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    newOption = slider.values.first().toInt()

                    newOption?.let { value ->
                        bind.tvRecQualityValue.text = RecPref.setTvRecQualityValue(value)
                        bind.tvQualityEstimate.text = setTvEstimateText(value)
                    }
                }
            })
        }
    }



    private fun setTvEstimateText(value : Int) : String  {

      return when(value){
            1 -> "1 minute ~ 600kb"
            2 -> "1 minute ~ 800kb"
            3 -> "1 minute ~ 1mb"
            4 -> "1 minute ~ 1.5mb"
            5 -> "1 minute ~ 2mb"
            else -> "1 minute ~ 2.8mb"
        }
    }


   private fun qualityIntToFloat(value : Int) : Float {

        return when(value){
            1 -> REC_QUALITY_LOW
            2 -> REC_QUALITY_MEDIUM
            3 -> REC_QUALITY_DEF
            4 -> REC_QUALITY_HIGH
            5 -> REC_QUALITY_ULTRA
            else -> REC_QUALITY_MAX
        }


    }


    override fun onStop() {
        super.onStop()
        _bind = null
    }

}