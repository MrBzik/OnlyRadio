package com.example.radioplayer.ui.animations

import android.animation.ValueAnimator
import android.view.View

class RvAlphaAnim(
) {

    private val anim = ValueAnimator.ofFloat(1f, 0f)

    fun startAnim(view: View){
        anim.addUpdateListener {
            view.alpha = anim.animatedValue as Float
        }

        anim.duration = 100
        anim.start()
    }


}