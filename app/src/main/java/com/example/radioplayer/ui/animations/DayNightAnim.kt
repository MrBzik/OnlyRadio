package com.example.radioplayer.ui.animations

import android.animation.ValueAnimator
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.radioplayer.R
import com.example.radioplayer.databinding.ActivityMainBinding
import com.example.radioplayer.databinding.StubPlayerActivityMainBinding
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.MainActivity.Companion.uiMode

const val FADE_OUT_DURATION = 500L
const val FADE_IN_DURATION = 700L


fun MainActivity.smoothDayNightFadeOut(){

    AlphaFadeOutAnim(1f, FADE_OUT_DURATION).apply {

        bindPlayer?.let {
            startAnim(it.root)
        }

        startAnim(bind.bottomNavigationView)

        bind.viewSeparatorStart?.let{
            startAnim(it)
            startAnim(bind.viewSeparatorEnd!!)
            startAnim(bind.separatorSecond!!)
            startAnim(bind.separatorLowest!!)
        }
    }

    if(uiMode != Configuration.UI_MODE_NIGHT_YES){

        val colorAnimator = ValueAnimator.ofArgb(Color.WHITE, Color.BLACK)
        colorAnimator.addUpdateListener {
            bind.root.setBackgroundColor(it.animatedValue as Int)
        }
        colorAnimator.duration = FADE_OUT_DURATION
        colorAnimator.start()

        val colorFrom = ContextCompat.getColor(this, R.color.nav_bar_settings_frag)

        val barsColorAnimator = ValueAnimator.ofArgb(colorFrom, Color.BLACK)
        barsColorAnimator.addUpdateListener { value ->
                window.apply {
                navigationBarColor = value.animatedValue as Int
                statusBarColor = value.animatedValue as Int
            }
        }
        barsColorAnimator.duration = FADE_OUT_DURATION
        barsColorAnimator.start()
    }
}

fun MainActivity.smoothDayNightFadeIn(){

    bindPlayer?.root?.slideAnim(FADE_IN_DURATION, 0, R.anim.fade_in_anim)

    bind.bottomNavigationView.slideAnim(FADE_IN_DURATION, 0, R.anim.fade_in_anim)
    bind.viewSeparatorStart?.slideAnim(FADE_IN_DURATION, 0, R.anim.fade_in_anim)
    bind.viewSeparatorEnd?.slideAnim(FADE_IN_DURATION, 0, R.anim.fade_in_anim)
    bind.separatorSecond?.slideAnim(FADE_IN_DURATION, 0, R.anim.fade_in_anim)
    bind.separatorLowest?.slideAnim(FADE_IN_DURATION, 0, R.anim.fade_in_anim)

    if(uiMode != Configuration.UI_MODE_NIGHT_YES){

        val colorAnimator = ValueAnimator.ofArgb(Color.BLACK, Color.WHITE)
        colorAnimator.addUpdateListener {
            bind.rootLayout.setBackgroundColor(it.animatedValue as Int)
        }
        colorAnimator.duration = FADE_IN_DURATION
        colorAnimator.start()

        val colorTo = ContextCompat.getColor(bind.root.context, R.color.nav_bar_settings_frag)

        val barsColorAnimator = ValueAnimator.ofArgb(Color.BLACK, colorTo)
        barsColorAnimator.addUpdateListener { value ->

                window.apply {
                navigationBarColor = value.animatedValue as Int
                statusBarColor = value.animatedValue as Int
            }
        }
        barsColorAnimator.duration = FADE_IN_DURATION
        barsColorAnimator.start()

    }
}
