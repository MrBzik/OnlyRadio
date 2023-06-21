package com.example.radioplayer.ui.animations

import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.example.radioplayer.R
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.utils.Constants.FRAG_HISTORY
import com.example.radioplayer.utils.Constants.FRAG_OPTIONS
import com.example.radioplayer.utils.TextViewOutlined

object SwapTitlesUi {


    fun swap(conditionA : Boolean,
             textViewA : TextView,
             textViewB : TextView,
             isToAnimate : Boolean,
             toolbar : View,
             fragment : Int
             ){


        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
            if(conditionA){
                textViewB.setTextAppearance(R.style.unselectedTitle)
                textViewA.setTextAppearance(R.style.selectedTitle)

                if(isToAnimate){
                    textViewB.objectSizeScaleAnimation(18f, 15f)
                    textViewA.objectSizeScaleAnimation(15f, 18f)
                }
            } else {

                textViewB.setTextAppearance(R.style.selectedTitle)
                textViewA.setTextAppearance(R.style.unselectedTitle)

                if(isToAnimate){
                    textViewB.objectSizeScaleAnimation(15f, 18f)
                    textViewA.objectSizeScaleAnimation(18f, 15f)
                }
            }
        } else {

            if(conditionA){

                val drawable = when (fragment){
                    FRAG_HISTORY -> R.drawable.toolbar_history_stations_protected
                    FRAG_OPTIONS -> R.drawable.toolbar_settings_extras_vector
                    else -> 0
                }

                toolbar.setBackgroundResource(drawable)

                (textViewA as TextViewOutlined).apply {
                    isSingleColor = true
                    setTextColor(Color.BLACK)
                }

                (textViewB as TextViewOutlined).apply {
                    isSingleColor = false
                    invalidate()
                }

            } else {

                val drawable = when (fragment){
                    FRAG_HISTORY -> R.drawable.toolbar_history_titles_vector
                    FRAG_OPTIONS -> R.drawable.toolbar_settings_vector
                    else -> 0
                }

                toolbar.setBackgroundResource(drawable)

                (textViewB as TextViewOutlined).apply {
                    isSingleColor = true
                    setTextColor(Color.BLACK)
                }
                (textViewA as TextViewOutlined).apply {
                    isSingleColor = false
                    invalidate()
                }
            }
        }
    }
}