package com.example.radioplayer.exoPlayer.callbacks

import android.app.Notification
import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.Intent
import android.os.Build
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

            if(Build.VERSION.SDK_INT>=24) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                stopForeground(true)
            }
            isForegroundService = false
            stopSelf()
        }

    }

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

                startForeground(NOTIFICATION_ID, notification)
                isForegroundService = true
            }
        }
    }
}