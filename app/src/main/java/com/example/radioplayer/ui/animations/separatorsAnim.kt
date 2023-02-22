package com.example.radioplayer.ui.animations

import android.R
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable


fun animateSeparator(v: Drawable, context : Context) {
    val orange: Int = context.getResources().getColor(R.color.holo_orange_dark)
    val colorAnim = ObjectAnimator.ofFloat(0f, 1f)
    colorAnim.addUpdateListener { animation ->
        val mul = animation.animatedValue as Float
        val alphaOrange = adjustAlpha(orange, mul)
        v.setColorFilter(alphaOrange, PorterDuff.Mode.SRC_ATOP)
        if (mul.toDouble() == 0.0) {
            v.setColorFilter(null)
        }
    }
    colorAnim.duration = 1500
    colorAnim.repeatMode = ValueAnimator.REVERSE
    colorAnim.repeatCount = -1
    colorAnim.start()
}

fun adjustAlpha(color: Int, factor: Float): Int {
    val alpha = Math.round(Color.alpha(color) * factor).toInt()
    val red: Int = Color.red(color)
    val green: Int = Color.green(color)
    val blue: Int = Color.blue(color)
    return Color.argb(alpha, red, green, blue)
}