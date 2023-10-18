package com.onlyradio.radioplayer.ui.dialogs

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.databinding.DialogSearchParamsBinding
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.ui.viewmodels.MainViewModel
import com.onlyradio.radioplayer.ui.viewmodels.SearchDialogsViewModel
import com.google.android.material.slider.RangeSlider
import java.util.Locale


const val ORDER_VOTES = "Top voted"
const val ORDER_POP = "Most popular"
const val ORDER_TREND = "Trending now"
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





val bitrateCalclList = arrayListOf(8000, 60, 250, 30, 1350, 2850, 3800, 550, 2050, 500, 15100, 850, 3400, 150, 800, 50, 1700, 450, 0)

class SearchParamsDialog (
    private val requireContext : Context,
    private val mainViewModel: MainViewModel,
    private val searchDialogsViewModel : SearchDialogsViewModel,
    private val handleNewParams : () -> Unit
        ) : BaseDialog<DialogSearchParamsBinding>(
    requireContext, DialogSearchParamsBinding::inflate
        )
{

    lateinit var orderAdapter : ArrayAdapter<String>

    private var newOrderSetting : String? = null


//
//    private val listOfOrder =
//        listOf(ORDER_VOTES, ORDER_POP, ORDER_TREND,
//            ORDER_RANDOM)

    private var stationsCount = ""
    private var systemStr = ""
    private var maxValue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        maxValue = requireContext.resources.getString(R.string.maximum_value)

        setStrings()

        setOrderExpander()

        setRangeSliderBitrate()

        setAcceptButton()

        setLanguageOption()

        updateStationsCountForLanguage()

        bind.tvBack.setOnClickListener {
            dismiss()
        }

    }



    private fun setStrings(){
        stationsCount = context.resources.getString(R.string.stations_count)
        systemStr = context.resources.getString(R.string.system)
    }

    private fun updateStationsCountForLanguage(){

        searchDialogsViewModel.updateLanguageCount {
            bind.tvStationsLangCount.text = "$it $stationsCount"
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

            handleNewParams()

            dismiss()

        }
    }


    private fun setLanguageOption(){

        bind.tvLanguageValue.text = "$systemStr: ${Locale.getDefault().displayLanguage}"

        bind.switchLanguagePref.isChecked = mainViewModel.isSearchFilterLanguage

    }


    private fun setOrderExpander(){

        bind.actvOrder.inputType = InputType.TYPE_NULL

        orderAdapter = ArrayAdapter(requireContext, R.layout.item_text_drop_down_menu,
            requireContext.resources.getStringArray(R.array.search_order_array))

        bind.actvOrder.apply {

            setAdapter(orderAdapter)

            val selection =  when(mainViewModel.newSearchOrder){
                ORDER_VOTES -> 0
                ORDER_POP -> 1
                ORDER_TREND -> 2
                else -> 3
            }

            setSelection(selection)

            setOnItemClickListener { _, _, position, _ ->

                newOrderSetting = when(position){
                    0 -> ORDER_VOTES
                    1 -> ORDER_POP
                    2 -> ORDER_TREND
                    else -> ORDER_RANDOM
                }

            }


            setText(orderAdapter.getItem(selection), false)

            if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
                setDropDownBackgroundResource(R.drawable.dialog_gradient_for_background)
            }
        }


    }


    private fun setRangeSliderBitrate(){
        bind.rangeSliderBitrate.apply {
            valueFrom = 0f
            valueTo = 9f
            stepSize = 1f

            val minBit = bitrateToFloatPosition(mainViewModel.minBitrateNew)
            val maxBit = bitrateToFloatPosition(mainViewModel.maxBitrateNew)

            values = listOf(minBit, maxBit)

            calcStationCountForBitrate()

            setLabelFormatter { value ->

                when(value){
                    0f -> maxValue
                    1f -> "$BITRATE_24 kbps"
                    2f -> "$BITRATE_32 kbps"
                    3f -> "$BITRATE_64 kbps"
                    4f ->"$BITRATE_96 kbps"
                    5f -> "$BITRATE_128 kbps"
                    6f -> "$BITRATE_192 kbps"
                    7f -> "$BITRATE_256 kbps"
                    8f -> "$BITRATE_320 kbps"
                    else -> maxValue
                }
            }


            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
                override fun onStartTrackingTouch(slider: RangeSlider) {

                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    val newMin = slider.values.first().toInt()
                    val newMax = slider.values[1].toInt()

                    if(newMin == newMax){
                        if( newMax == 0){
                            slider.values = listOf(0f, 1f)
                        } else if( newMax == 9){
                            slider.values = listOf(8f, 9f)
                        }
                    }

                    calcStationCountForBitrate()
                }
            })
        }
    }



    private fun calcStationCountForBitrate(){

        val bitMin = bind.rangeSliderBitrate.values.first().toInt()
        val bitMax = bind.rangeSliderBitrate.values[1].toInt()


        val indexFrom = bitMin*2
        val indexTo = bitMax*2
        var result = 0

        for(i in indexFrom .. indexTo){

           result += bitrateCalclList[i]

        }

        bind.tvStationsCountValue.text = "~ $result $stationsCount"


    }



    private fun bitrateToFloatPosition(bit : Int) : Float {

       return when(bit){
            BITRATE_0 -> 0f
            BITRATE_24 -> 1f
            BITRATE_32 -> 2f
            BITRATE_64 -> 3f
            BITRATE_96 -> 4f
            BITRATE_128 -> 5f
            BITRATE_192 -> 6f
            BITRATE_256 -> 7f
            BITRATE_320 -> 8f
            else -> 9f
        }
    }


    private fun floatPositionToBitrate(position : Float) : Int {

        return when(position){

            0f -> BITRATE_0
            1f -> BITRATE_24
            2f -> BITRATE_32
            3f -> BITRATE_64
            4f -> BITRATE_96
            5f -> BITRATE_128
            6f -> BITRATE_192
            7f -> BITRATE_256
            8f -> BITRATE_320
            else -> BITRATE_MAX

        }
    }


    override fun onStop() {
        super.onStop()
        bind.actvOrder.setAdapter(null)


        _bind = null
    }

}