package com.onlyradio.radioplayer.exoPlayer.callbacks

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.utils.Constants.NOTIFICATION_ID

class RadioPlayerNotificationListener (
    private val radioService : RadioService
        ) : PlayerNotificationManager.NotificationListener {


    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)

        radioService.apply {

//            Log.d("CHECKTAGS", "radio notification")

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

                if(Build.VERSION.SDK_INT >= 31){
                    try {
                        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
                        isForegroundService = true
                    } catch (e : ForegroundServiceStartNotAllowedException){/**/}
                }

                else {
                    startForeground(NOTIFICATION_ID, notification)
                    isForegroundService = true
                }
            }
        }
    }
}