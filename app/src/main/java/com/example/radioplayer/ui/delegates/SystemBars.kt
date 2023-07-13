package com.example.radioplayer.ui.delegates

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import com.example.radioplayer.R
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.utils.Constants.FRAG_FAV
import com.example.radioplayer.utils.Constants.FRAG_HISTORY
import com.example.radioplayer.utils.Constants.FRAG_REC
import com.example.radioplayer.utils.Constants.FRAG_SEARCH

interface SystemBars {

    fun setSystemBarsColor(context : Context, currentFrag: Int)
}

class SystemBarsImp : SystemBars{
    override fun setSystemBarsColor(context: Context, currentFrag: Int) {
        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){

            val color = when(currentFrag){
                FRAG_SEARCH -> ContextCompat.getColor(context, R.color.nav_bar_search_fragment)
                FRAG_FAV -> ContextCompat.getColor(context, R.color.nav_bar_fav_fragment)
                FRAG_HISTORY -> ContextCompat.getColor(context, R.color.nav_bar_history_frag)
                FRAG_REC -> ContextCompat.getColor(context, R.color.nav_bar_rec_frag)
                else -> ContextCompat.getColor(context, R.color.nav_bar_settings_frag)
            }

          if(context is ContextWrapper){
                (context.baseContext as Activity).window.apply {
                    navigationBarColor = color
                    statusBarColor = color
                }
            }
        }
    }
}