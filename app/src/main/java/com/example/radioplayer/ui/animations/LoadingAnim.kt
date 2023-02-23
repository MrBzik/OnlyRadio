package com.example.radioplayer.ui.animations

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat



class LoadingAnim(
    private val v : Drawable?,
    context: Context
) {

   private val red = ContextCompat.getColor(context, com.example.radioplayer.R.color.color_changed_on_interaction)
   private val orange = ContextCompat.getColor(context, com.example.radioplayer.R.color.Separator)

   private val anim = ValueAnimator.ofObject(ArgbEvaluator(), orange, red)

//   private var isToContinue = true


    fun startLoadingAnim() {

        anim.addUpdateListener {
            v?.colorFilter =
                BlendModeColorFilterCompat
                    .createBlendModeColorFilterCompat(anim.animatedValue as Int, BlendModeCompat.XOR)
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
            val endAnim = ValueAnimator.ofObject(ArgbEvaluator(), color, orange)
            endAnim.addUpdateListener {
                v?.colorFilter =
                    BlendModeColorFilterCompat
                        .createBlendModeColorFilterCompat(endAnim.animatedValue as Int, BlendModeCompat.XOR)
            }

            endAnim.duration = 800
            endAnim.start()
        }
    }
}







