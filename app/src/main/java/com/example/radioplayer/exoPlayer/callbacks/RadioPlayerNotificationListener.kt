package com.example.radioplayer.exoPlayer.callbacks

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.utils.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class RadioPlayerNotificationListener (
    private val radioService : RadioService
        ) : PlayerNotificationManager.NotificationListener {


    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)

        radioService.apply {

            Log.d("CHECKTAGS", "radio notification")

            stopForeground(Service.STOP_FOREGROUND_REMOVE)

            isForegroundService = false

            stopSelf()


        }
    }


//    @Suppress("DEPRECATION") // Deprecated for third party Services.
//    fun <T> Context.isServiceForegrounded(service: Class<T>) =
//        (getSystemService(ACTIVITY_SERVICE) as? ActivityManager)
//            ?.getRunningServices(Integer.MAX_VALUE)
//            ?.find { it.service.className == service.name }
//            ?.foreground == true


    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {


            super.onNotificationPosted(notificationId, notification, ongoing)


        radioService.apply {

            if(ongoing && !isForegroundService){

                ContextCompat.startForegroundService(this,
                    Intent(applicationContext, this::class.java)
                    )

//                startForegroundService()

                startForeground(NOTIFICATION_ID, notification)
                isForegroundService = true

            }
        }
    }
}