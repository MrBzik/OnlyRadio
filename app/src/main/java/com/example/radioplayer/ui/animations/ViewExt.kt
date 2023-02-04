package com.example.radioplayer.ui.animations

import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.example.radioplayer.R

fun View.slideAnim(duration : Long, offset : Long, anim : Int){
    val slideRight = AnimationUtils.loadAnimation(context, anim).apply {
        this.duration = duration
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = offset

    }

        startAnimation(slideRight)

}