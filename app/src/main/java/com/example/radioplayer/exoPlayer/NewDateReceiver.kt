package com.example.radioplayer.exoPlayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NewDateReceiver(private val newDateHandler : () -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_TIME_CHANGED ||
           intent?.action == Intent.ACTION_TIMEZONE_CHANGED ||
           intent?.action == Intent.ACTION_DATE_CHANGED){
            newDateHandler()
        }
    }




}