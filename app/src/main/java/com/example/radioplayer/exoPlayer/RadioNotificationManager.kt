package com.example.radioplayer.exoPlayer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.example.radioplayer.R
import com.example.radioplayer.utils.Constants.CHANNEL_ID
import com.example.radioplayer.utils.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener
import kotlinx.coroutines.*
import kotlin.math.log


const val ACTION_NEXT_STATION = "action next station"

class RadioNotificationManager (
    private val context : Context,
   sessionToken : MediaSessionCompat.Token,
   notificationListener: NotificationListener,
    private val glide : RequestManager,
    private val newSong : () -> CharSequence?
    ) {

    val color = ContextCompat.getColor(context, R.color.transparent)

//    private val newGlide : RequestManager = Glide.with(context)
//        .setDefaultRequestOptions(RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)
//            .fallback(R.drawable.ic_radio_default)
//            .placeholder(color)
//
//        )


    private val bitmap = ContextCompat.getDrawable(context, R.drawable.splash_screen)?.toBitmap(144, 144)

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val colorPlaceholder = ContextCompat.getColor(context, R.color.main_background)

    private val notificationManager : PlayerNotificationManager

    private val actionReceiver  = object : PlayerNotificationManager.CustomActionReceiver {
        override fun createCustomActions(
            context: Context,
            instanceId: Int
        ): MutableMap<String, NotificationCompat.Action> {
            return mutableMapOf(Pair(ACTION_NEXT_STATION,
                NotificationCompat.Action(R.drawable.ic_play_pause, "Next",
                PendingIntent.getBroadcast(context, 123,
                    Intent(ACTION_NEXT_STATION).setPackage(context.packageName), PendingIntent.FLAG_IMMUTABLE)
                    )
                ))
        }

        override fun getCustomActions(player: Player): MutableList<String> {
           return mutableListOf(ACTION_NEXT_STATION)
        }

        override fun onCustomAction(player: Player, action: String, intent: Intent) {
            if(action == ACTION_NEXT_STATION){
                Log.d("CHECKTAGS", "next action")
            }
        }
    }

    fun clearServiceJob(){
        serviceJob.cancel()
    }


        init {

            val mediaController = MediaControllerCompat(context, sessionToken)

            notificationManager = PlayerNotificationManager.Builder(
                context,
                NOTIFICATION_ID,
               CHANNEL_ID
            ).setChannelNameResourceId(R.string.notification_channel_name)
                .setChannelDescriptionResourceId(R.string.notification_channel_description)
                .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
                .setNotificationListener(notificationListener)
//                .setCustomActionReceiver(actionReceiver)
                .build().apply {
                    setSmallIcon(R.drawable.ic_radio_default)
                    setMediaSessionToken(sessionToken)
                    setUseChronometer(false)
                    setUseNextActionInCompactView(true)
                    setUsePreviousActionInCompactView(true)
//                    setUseNextAction(true)
//                    setUsePreviousAction(true)
                    setColor(colorPlaceholder)
                }


        }

    fun showNotification(player : Player){

        notificationManager.setPlayer(player)
    }

    fun removeNotification () {
        notificationManager.setPlayer(null)
    }


    fun updateNotification(){

        notificationManager.invalidate()
    }



    var currentIconUri: Uri? = null
    var currentBitmap: Bitmap? = null

    private inner class DescriptionAdapter (
        private val mediaController : MediaControllerCompat
            ) : MediaDescriptionAdapter {


        override fun getCurrentContentTitle(player: Player): CharSequence {
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {

            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return RadioService.currenlyPlaingSong
//            return newSong()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val iconUri = mediaController.metadata.description.iconUri
            return if(iconUri == null){
                currentBitmap = bitmap
                currentIconUri = null
                bitmap
            }
            else if (currentIconUri != iconUri || currentBitmap == null) {

                // Cache the bitmap for the current song so that successive calls to
                // `getCurrentLargeIcon` don't cause the bitmap to be recreated.
                currentIconUri = iconUri
                serviceScope.launch {
                    currentBitmap = resolveUriAsBitmap(iconUri)
                    currentBitmap?.let { callback.onBitmap(it) }
                    updateNotification()
                }

                currentBitmap
            } else {
                currentIconUri = iconUri
                currentBitmap
            }
        }


        private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap? {
            return try {
                withContext(Dispatchers.IO) {
                    // Block on downloading artwork.
                    glide
                        .asBitmap()
                        .load(uri)
                        .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
                        .get()

                }
            } catch (e:Exception){
                currentBitmap = bitmap
                bitmap
            }
        }
    }
}

const val NOTIFICATION_LARGE_ICON_SIZE = 144 // px
