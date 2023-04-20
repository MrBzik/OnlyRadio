package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.example.radioplayer.R
import com.example.radioplayer.databinding.DialogSearchParamsBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.google.android.material.slider.RangeSlider
import java.util.*


const val ORDER_VOTES = "Top voted"
const val ORDER_POP = "Most popular"
const val ORDER_TREND = "Trending now"
const val ORDER_BIT_MIN = "Bitrate (up)"
const val ORDER_BIT_MAX = "Bitrate (down)"
const val ORDER_RANDOM = "Random order"

const val BITRATE_0 = 0
const val BITRATE_24 = 24
const val BITRATE_32 = 32
const val BITRATE_64 = 64
const val BITRATE_96 = 96
const val BITRATE_128 = 128
const val BITRATE_192 = 192
const val BITRATE_256 = 256
const val BITRATE_320 = 320
const val BITRATE_MAX = 1000000




class SearchParamsDialog (
    private val requireContext : Context,
    private val mainViewModel: MainViewModel
        ) : BaseDialog<DialogSearchParamsBinding>(
    requireContext, DialogSearchParamsBinding::inflate
        )
{

    lateinit var orderAdapter : ArrayAdapter<String>

    private var newOrderSetting : String? = null



    private val listOfOrder =
        listOf(ORDER_VOTES, ORDER_POP, ORDER_TREND,
//            ORDER_BIT_MIN, ORDER_BIT_MAX,
            ORDER_RANDOM)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setOrderExpander()

        setRangeSliderBitrate()

        setAcceptButton()

        setLanguageOption()

        bind.tvBack.setOnClickListener {
            dismiss()
        }




    }

    private fun setAcceptButton(){

        bind.tvAccept.setOnClickListener {

            newOrderSetting?.let {
                mainViewModel.newSearchOrder = it
            }

            mainViewModel.minBitrateNew = floatPositionToBitrate(
                bind.rangeSliderBitrate.values.first()
            )

            mainViewModel.maxBitrateNew = floatPositionToBitrate(
                bind.rangeSliderBitrate.values[1]
            )

            mainViewModel.isSearchFilterLanguage = bind.switchLanguagePref.isChecked


            dismiss()

        }
    }


    private fun setLanguageOption(){

        bind.tvLanguageValue.text = "System: ${Locale.getDefault().displayLanguage}"

        bind.switchLanguagePref.isChecked = mainViewModel.isSearchFilterLanguage

    }


    private fun setOrderExpander(){

        bind.actvOrder.inputType = InputType.TYPE_NULL

        orderAdapter = ArrayAdapter(requireContext, R.layout.item_text_drop_down_menu, listOfOrder)

        bind.actvOrder.apply {

            setAdapter(orderAdapter)

            setSelection(

                when(mainViewModel.newSearchOrder){
                    ORDER_VOTES -> 0
                    ORDER_POP -> 1
                    ORDER_TREND -> 2
//                    ORDER_BIT_MIN -> 3
//                    ORDER_BIT_MAX -> 4
                    else -> 3
                }
            )

            setOnItemClickListener { _, _, position, _ ->

                newOrderSetting = orderAdapter.getItem(position)
            }


            setText(mainViewModel.newSearchOrder, false)

            if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
                setDropDownBackgroundResource(R.drawable.dialog_gradient_for_background)
            }
        }


    }


    private fun setRangeSliderBitrate(){
        bind.rangeSliderBitrate.apply {
            valueFrom = 1f
            valueTo = 10f
            stepSize = 1f

            val minBit = bitrateToFloatPosition(mainViewModel.minBitrateNew)
            val maxBit = bitrateToFloatPosition(mainViewModel.maxBitrateNew)

            values = listOf(minBit, maxBit)

            setLabelFormatter { value ->

                when(value){
                    1f -> "Unset"
                    2f -> "$BITRATE_24 kbps"
                    3f -> "$BITRATE_32 kbps"
                    4f -> "$BITRATE_64 kbps"
                    5f ->"$BITRATE_96 kbps"
                    6f -> "$BITRATE_128 kbps"
                    7f -> "$BITRATE_192 kbps"
                    8f -> "$BITRATE_256 kbps"
                    9f -> "$BITRATE_320 kbps"
                    else -> "Unset"
                }
            }


            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
                override fun onStartTrackingTouch(slider: RangeSlider) {

                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    val newMin = slider.values.first().toInt()
                    val newMax = slider.values[1].toInt()

                    if(newMin == newMax){
                        if( newMax == 1){
                            slider.values = listOf(1f, 2f)
                        } else if( newMax == 10){
                            slider.values = listOf(9f, 10f)
                        }

                    }

                }
            })


        }
    }


    private fun bitrateToFloatPosition(bit : Int) : Float {

       return when(bit){
            BITRATE_0 -> 1f
            BITRATE_24 -> 2f
            BITRATE_32 -> 3f
            BITRATE_64 -> 4f
            BITRATE_96 -> 5f
            BITRATE_128 -> 6f
            BITRATE_192 -> 7f
            BITRATE_256 -> 8f
            BITRATE_320 -> 9f
            else -> 10f
        }
    }


    private fun floatPositionToBitrate(position : Float) : Int {

        return when(position){

            1f -> BITRATE_0
            2f -> BITRATE_24
            3f -> BITRATE_32
            4f -> BITRATE_64
            5f -> BITRATE_96
            6f -> BITRATE_128
            7f -> BITRATE_192
            8f -> BITRATE_256
            9f -> BITRATE_320
            else -> BITRATE_MAX

        }
    }


    override fun onStop() {
        super.onStop()
        bind.actvOrder.setAdapter(null)


        _bind = null
    }

}