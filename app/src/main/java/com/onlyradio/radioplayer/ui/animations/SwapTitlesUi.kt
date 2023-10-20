package com.onlyradio.radioplayer.ui.animations

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.utils.Constants.FRAG_HISTORY
import com.onlyradio.radioplayer.utils.Constants.FRAG_OPTIONS
import com.onlyradio.radioplayer.utils.TextViewOutlined

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
                when (fragment){
                    FRAG_HISTORY -> {
                      if(isToAnimate) {
                          (ContextCompat.getDrawable(toolbar.context, R.drawable.toolbar_animated_history_stations)
                                  as AnimatedVectorDrawable).apply {
                                  toolbar.background = this
                                  start()
                              }
                      } else toolbar.setBackgroundResource(R.drawable.toolbar_history_stations_vector)
                    }
                    FRAG_OPTIONS -> {
                        if(isToAnimate){
                            (ContextCompat.getDrawable(toolbar.context, R.drawable.toolbar_animated_settings_extras)
                                    as AnimatedVectorDrawable).apply {
                                        toolbar.background = this
                                        start()
                                }
                        } else toolbar.setBackgroundResource(R.drawable.toolbar_settings_extras_vector)
                    }
                }

                (textViewA as TextViewOutlined).apply {
                    isSingleColor = true
                    setTextColor(Color.BLACK)
                }

                (textViewB as TextViewOutlined).apply {
                    isSingleColor = false
                    invalidate()
                }

            } else {
                when (fragment){
                    FRAG_HISTORY -> {
                        if(isToAnimate) {
                            (ContextCompat.getDrawable(toolbar.context, R.drawable.toolbar_animated_history_titles)
                                    as AnimatedVectorDrawable).apply {
                                toolbar.background = this
                                start()
                            }
                        } else toolbar.setBackgroundResource(R.drawable.toolbar_history_titles_vector)
                    }
                    FRAG_OPTIONS -> {
                        if(isToAnimate){
                            (ContextCompat.getDrawable(toolbar.context, R.drawable.toolbar_animated_settings_general)
                                    as AnimatedVectorDrawable).apply {
                                toolbar.background = this
                                start()
                            }
                        } else toolbar.setBackgroundResource(R.drawable.toolbar_settings_vector)
                    }
                }


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