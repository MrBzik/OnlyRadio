package com.onlyradio.radioplayer.ui.animations

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.widget.TextView

fun TextView.sizeScaleAnimation(startSize : Float, endSize: Float) {
    val animator = ValueAnimator.ofFloat(startSize, endSize)

    animator.addUpdateListener {
        this.textSize = it.animatedValue as Float
    }

    animator.duration = 300L
    animator.start()
}

fun TextView.objectSizeScaleAnimation(startSize : Float, endSize: Float) {
    val animator = ObjectAnimator.ofFloat(this, "textSize", startSize, endSize)
    animator.duration = 150
    animator.start()
}