package com.onlyradio.radioplayer.ui.dialogs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.slider.RangeSlider
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.databinding.DialogBufferSettingsBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.utils.Constants.BUFFER_FOR_PLAYBACK
import com.onlyradio.radioplayer.utils.Constants.BUFFER_SIZE_IN_MILLS
import com.onlyradio.radioplayer.utils.Constants.IS_ADAPTIVE_LOADER_TO_USE


const val MAX_PLAYBACK_BUFFER = 30000f
class BufferSettingsDialog (
    private val requireContext : Context,
    private val buffPref : SharedPreferences,
    private val handleResult : (Boolean) -> Unit
        ) : BaseDialog<DialogBufferSettingsBinding> (
            requireContext,
    DialogBufferSettingsBinding::inflate
                ){

    private var secSuffix = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        secSuffix = requireContext.getString(R.string.buffer_setting_sec)

//        adjustDialogHeight(bind.clBufferSettingDialog)

        setRangeBufferInSec()

//        setRangeBufferInBytes()

        setRangePlaybackBuffer()

        initialSwitchButtonsState()

        setApplyButton()

        bind.tvBack.setOnClickListener {
            dismiss()
        }

//        setBufferBytesTitleColor()
        setLoadControllerTitleColor()

    }

    private fun initialSwitchButtonsState(){

//        bind.switchInBytes.isChecked = RadioService.isToSetBufferInBytes
        bind.switchLoadController.isChecked = RadioService.isAdaptiveLoaderToUse
    }


    private fun setRangeBufferInSec(){
        bind.rangeSliderBufferInSeconds.apply {
            valueFrom = 5f
            valueTo = 100f
            stepSize = 1f

            val initialValue = RadioService.bufferSizeInMills / 1000

            bind.tvBufferInSecondsTitle.text = "$initialValue $secSuffix"

            values = listOf(initialValue .toFloat())

            setLabelFormatter { value ->
                "${value.toInt()} $secSuffix"
            }


           addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
                override fun onStartTrackingTouch(slider: RangeSlider) {

                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    val newValue = slider.values.first().toInt()

                    bind.tvBufferInSecondsTitle.text = "$newValue $secSuffix"

                    setRangePlaybackBuffer(
                        maxValue = (newValue * 1000).toFloat(),
                        initialValue = bind.rangeSliderPlaybackBuffer.values.first().toInt(),
                        isRecreated = true
                    )

                }
            })


        }
    }

//    private fun setRangeBufferInBytes(){
//        bind.rangeSliderBufferInBytes.apply {
//            valueFrom = 300f
//            valueTo = 3000f
//            stepSize = 100f
//
//            val initialValue = if(RadioService.bufferSizeInBytes > 0) RadioService.bufferSizeInBytes
//            else 300
//
//            bind.tvBufferInBytesTitle.text = "$initialValue kilobytes"
//
//            values = listOf(initialValue.toFloat())
//
//            setLabelFormatter { value ->
//                "${value.toInt()} kilobytes"
//            }
//
//            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
//                override fun onStartTrackingTouch(slider: RangeSlider) {
//
//                }
//
//                override fun onStopTrackingTouch(slider: RangeSlider) {
//                    val newValue = slider.values.first().toInt()
//
//                    bind.tvBufferInBytesTitle.text = "$newValue kilobytes"
//                }
//            })
//
//        }
//    }



    private fun setRangePlaybackBuffer(
        maxValue : Float = MAX_PLAYBACK_BUFFER,
        initialValue : Int = RadioService.bufferForPlayback,
        isRecreated : Boolean = false
    ){
        bind.rangeSliderPlaybackBuffer.apply {
            valueFrom = 1000f
            valueTo = minOf(maxValue, MAX_PLAYBACK_BUFFER)
            stepSize = 1000f

            val minValue = minOf(initialValue.toFloat(), valueTo)

            bind.tvPlaybackBufferValue.text = "${minValue.toInt() / 1000} $secSuffix"

            values = listOf(minValue)

            if(isRecreated) return


            setLabelFormatter { value ->
                "${value.toInt() / 1000} $secSuffix"
            }

            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
                override fun onStartTrackingTouch(slider: RangeSlider) {

                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    val newValue = slider.values.first().toInt()

                    bind.tvPlaybackBufferValue.text = "${newValue / 1000} $secSuffix"
                }
            })
        }
    }

//    private fun setBufferBytesTitleColor(){
//
//        toggleTitleColors(RadioService.isToSetBufferInBytes, bind.tvBufferInBytesTitle)
//
//        bind.switchInBytes.setOnCheckedChangeListener { _, isChecked ->
//
//            toggleTitleColors(isChecked, bind.tvBufferInBytesTitle)
//        }
//    }

    private fun setLoadControllerTitleColor(){

        toggleTitleColors(RadioService.isAdaptiveLoaderToUse, bind.tvLoadCallback)

        bind.switchLoadController.setOnCheckedChangeListener { _, isChecked ->

            toggleTitleColors(isChecked, bind.tvLoadCallback)
        }
    }


    private fun toggleTitleColors(isActive : Boolean, textView : TextView){

        if(isActive){
            textView.setTextColor(
                ContextCompat.getColor(requireContext, R.color.selected_genre_color)
            )
        } else {
            textView.setTextColor(
                ContextCompat.getColor(requireContext, R.color.unselected_genre_color)
            )
        }
    }



    private fun setApplyButton(){

        bind.tvAccept.setOnClickListener {

            var isPlayerRestartNeeded = false

            buffPref.edit().apply {

                val millsSize = bind.rangeSliderBufferInSeconds.values.first().toInt() * 1000

                if(RadioService.bufferSizeInMills != millsSize){
                    putInt(BUFFER_SIZE_IN_MILLS, millsSize)
                    RadioService.bufferSizeInMills = millsSize
                    isPlayerRestartNeeded = true
                }

//                val bytesSize = bind.rangeSliderBufferInBytes.values.first().toInt()
//                val isSwitchBytesChecked = bind.switchInBytes.isChecked

//                if(RadioService.bufferSizeInBytes != bytesSize){
//                    putInt(BUFFER_SIZE_IN_BYTES, bytesSize)
//                    RadioService.bufferSizeInBytes = bytesSize
//                    if(isSwitchBytesChecked){
//                        isPlayerRestartNeeded = true
//                    }
//                }

//                if(RadioService.isToSetBufferInBytes != isSwitchBytesChecked){
//                    RadioService.isToSetBufferInBytes = isSwitchBytesChecked
//                    putBoolean(IS_TO_SET_BUFFER_IN_BYTES, isSwitchBytesChecked)
//                    isPlayerRestartNeeded = true
//                }


                val millsPlayback = bind.rangeSliderPlaybackBuffer.values.first().toInt()

                if(RadioService.bufferForPlayback != millsPlayback){
                    putInt(BUFFER_FOR_PLAYBACK, millsPlayback)
                    RadioService.bufferForPlayback = millsPlayback
                    isPlayerRestartNeeded = true
                }

                val checkAdaptiveLoader = bind.switchLoadController.isChecked

                if(RadioService.isAdaptiveLoaderToUse != checkAdaptiveLoader){
                    RadioService.isAdaptiveLoaderToUse = checkAdaptiveLoader
                    putBoolean(IS_ADAPTIVE_LOADER_TO_USE, checkAdaptiveLoader)
                }

            }.apply()

            handleResult(isPlayerRestartNeeded)

            dismiss()
        }
    }





    override fun onStop() {
        super.onStop()
        _bind = null
    }


}