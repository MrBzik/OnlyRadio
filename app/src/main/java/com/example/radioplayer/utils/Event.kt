package com.example.radioplayer.utils

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

