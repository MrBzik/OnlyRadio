package com.example.radioplayer.ui.animations

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat

class TextLoadAnim(
    private val context : Context,
    private val textView : TextView
) {

    private val activeColor = ContextCompat.getColor(context, com.example.radioplayer.R.color.separatorLoad)
    private val defaultColor = ContextCompat.getColor(context, com.example.radioplayer.R.color.transparent)

    private val anim = ValueAnimator.ofObject(ArgbEvaluator(), defaultColor, activeColor)


    fun startLoadingAnim() {

        anim.addUpdateListener {
            textView.setTextColor(it.animatedValue as Int)
        }
        anim.duration = 1500
        anim.repeatMode = ValueAnimator.REVERSE
        anim.repeatCount = -1
        anim.start()
    }

    fun endLoadingAnim(){

        if(anim.isRunning){
            val color = anim.animatedValue as Int
            anim.cancel()

            val endAnim = ValueAnimator.ofObject(ArgbEvaluator(), color, defaultColor)

            endAnim.addUpdateListener {
                textView.setTextColor(it.animatedValue as Int)
            }

            endAnim.duration = 800
            endAnim.start()
        }
    }

}