package com.example.radioplayer.ui.animations

import android.view.View
import com.example.radioplayer.R

class AdapterAnimator {

    private var isToAnimate = true
    private var count = 0

    private var DELAY = 200

    fun animateAppearance(view : View){

        if(isToAnimate){
//            Log.d("CHECKTAGS", "delay is ${(count * 35).toLong() + DELAY}")
            view.slideAnim(350, (count * 35).toLong() + DELAY, R.anim.fall_down)
            count ++
        } else {

         view.slideAnim(350, 0, R.anim.fade_in_anim)

        }
    }

    fun cancelAnimator(){
        isToAnimate = false
        count = 0
    }

    fun resetAnimator(){
        isToAnimate = true
        count = 0
    }
}