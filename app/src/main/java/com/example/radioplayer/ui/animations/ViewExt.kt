package com.example.radioplayer.ui.animations

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.animation.doOnEnd
import androidx.interpolator.view.animation.FastOutSlowInInterpolator


fun View.slideAnim(duration : Long, offset : Long, anim : Int){
    val slideRight = AnimationUtils.loadAnimation(context, anim).apply {
        this.duration = duration
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = offset

    }
        startAnimation(slideRight)
}


fun View.fadeOut(duration: Long, startAlpha: Float, oldPosition : Int, checkValidness : (Int) -> Unit) {
    val anim = ValueAnimator.ofFloat(startAlpha, 0f)
    anim.addUpdateListener {
        alpha = anim.animatedValue as Float
    }
    anim.duration = duration
    anim.start()
    anim.doOnEnd {
            checkValidness(oldPosition)
    }
}

