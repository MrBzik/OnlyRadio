package com.example.radioplayer.ui.animations

import android.animation.ValueAnimator
import android.view.View

class AlphaFadeOutAnim(
   private val startVal : Float,
   private val duration : Long
) {

    private val anim = ValueAnimator.ofFloat(startVal, 0f)

    fun startAnim(view: View){
        anim.addUpdateListener {
            view.alpha = anim.animatedValue as Float
        }

        anim.duration = duration
        anim.start()
    }

}