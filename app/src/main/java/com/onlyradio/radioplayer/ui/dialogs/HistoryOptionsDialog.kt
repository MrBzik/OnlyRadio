package com.onlyradio.radioplayer.ui.dialogs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.onlyradio.radioplayer.databinding.DialogHistoryOptionsBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.utils.Constants
import com.onlyradio.radioplayer.utils.Constants.HISTORY_PREF_BOOKMARK
import com.onlyradio.radioplayer.utils.Constants.HISTORY_PREF_DATES
import com.google.android.material.slider.RangeSlider

class HistoryOptionsDialog (
    requireContext : Context,
    private val historyPref : SharedPreferences,
    private val cleanupCheck : () -> Unit
        ) : BaseDialog<DialogHistoryOptionsBinding> (
            requireContext,
    DialogHistoryOptionsBinding::inflate
                ){


    private val initialDatesNumber = historyPref.getInt(HISTORY_PREF_DATES,
        Constants.HISTORY_DATES_PREF_DEFAULT
    )
    private val initialBookmarkNumber =  RadioService.historyPrefBookmark

    private var newDatesNumber = initialDatesNumber
    private var newBookmarkNumber = initialBookmarkNumber


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setRangeHistoryDates()

        setRangeBookmarkNumber()

        setApplyButton()

        bind.tvBack.setOnClickListener {
            dismiss()
        }
    }



    private fun setRangeHistoryDates(){
        bind.rangeSliderHistoryDates.apply {
            valueFrom = 1f
            valueTo = 31f
            stepSize = 2f


            bind.tvHistoryDatesValue.text = getDatesValue(initialDatesNumber)

            values = listOf(initialDatesNumber.toFloat())

            setLabelFormatter { value ->
                getDatesValue(value.toInt())
            }


           addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
                override fun onStartTrackingTouch(slider: RangeSlider) {

                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    newDatesNumber = slider.values.first().toInt()


                    bind.tvHistoryDatesValue.text = getDatesValue(newDatesNumber)

                    if(newDatesNumber < initialDatesNumber){
                        bind.tvWarning.visibility = View.VISIBLE
                    } else if(newBookmarkNumber >= initialBookmarkNumber){
                        bind.tvWarning.visibility = View.INVISIBLE
                    }

                }
            })
        }
    }

    private fun getDatesValue(value : Int) : String {
        return  if(value == 1){
            "$value date"
        } else {
            "$value dates"
        }
    }


    private fun setRangeBookmarkNumber(){
        bind.rangeSliderBookmarkNumber.apply {
            valueFrom = 10f
            valueTo = 100f
            stepSize = 5f

            bind.tvBookmarkNumberValue.text = getBookmarkValTitle(initialBookmarkNumber)

            values = listOf(initialBookmarkNumber.toFloat())

            setLabelFormatter { value ->
                getBookmarkVal(value.toInt())

            }

            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
                override fun onStartTrackingTouch(slider: RangeSlider) {

                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    newBookmarkNumber = slider.values.first().toInt()

                    bind.tvBookmarkNumberValue.text = getBookmarkValTitle(newBookmarkNumber)

                    if(newBookmarkNumber < initialBookmarkNumber){
                        bind.tvWarning.visibility = View.VISIBLE
                    } else if(newDatesNumber >= initialDatesNumber){
                        bind.tvWarning.visibility = View.INVISIBLE
                    }
                }
            })
        }
    }

    private fun getBookmarkValTitle(value : Int) : String {

        return if(value == 100){
                "unlimited"
        } else {
                value.toString()
        }
    }


    private fun getBookmarkVal(value : Int) : String {

        return if(value == 100){
                "Unlimited"
        } else {
            "$value titles"
        }
    }

    private fun setApplyButton(){

        bind.tvAccept.setOnClickListener {

            if(newBookmarkNumber != initialBookmarkNumber) {

               historyPref.edit().putInt(HISTORY_PREF_BOOKMARK, newBookmarkNumber).apply()

               RadioService.historyPrefBookmark = newBookmarkNumber

               if(newBookmarkNumber < initialBookmarkNumber){
                   cleanupCheck()
               }

            }


            if(newDatesNumber != initialDatesNumber){

               historyPref.edit().putInt(HISTORY_PREF_DATES, newDatesNumber).apply()
//                if(newDatesNumber < initialDatesNumber){
//                    mainViewModel.compareDatesWithPrefAndCLeanIfNeeded()
//                }
            }

            dismiss()
        }
    }



    override fun onStop() {
        super.onStop()
        _bind = null
    }


}