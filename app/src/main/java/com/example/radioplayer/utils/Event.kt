package com.example.radioplayer.utils

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.bottomnavigation.BottomNavigationView

open class Event <out T> (private val data : T){

   private  var hasBeenHandled = false
    private set

    fun getContentIfNotHandled () : T? {

        return if(hasBeenHandled) {
            null
        } else{
            hasBeenHandled = true
            data
        }
    }

        fun peekContent () = data
}

