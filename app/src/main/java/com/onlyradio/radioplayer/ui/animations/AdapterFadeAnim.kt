package com.onlyradio.radioplayer.ui.animations

import android.view.View
import com.onlyradio.radioplayer.R

object AdapterFadeAnim {

     fun adapterItemFadeIn (view : View){
            view.slideAnim(350, 0, R.anim.fade_in_anim)
    }

}