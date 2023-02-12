package com.example.radioplayer.exoPlayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.radioplayer.R
import com.example.radioplayer.utils.Constants.CHANNEL_ID
import com.example.radioplayer.utils.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener
import kotlinx.coroutines.*


class RadioNotificationManager (
    private val context : Context,
   sessionToken : MediaSessionCompat.Token,
   notificationListener: NotificationListener
    ) {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val notificationManager : PlayerNotificationManager


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
                .build().apply {
                    setSmallIcon(R.drawable.ic_radio_default)
                    setMediaSessionToken(sessionToken)
                    setUseChronometer(false)
                    setColor(Color.BLACK)
                }


        }

    fun showNotification(player : Player){

        notificationManager.setPlayer(player)
    }

    fun removeNotification () {
        notificationManager.setPlayer(null)
    }


    private inner class DescriptionAdapter (
        private val mediaController : MediaControllerCompat
            ) : MediaDescriptionAdapter {

        var currentIconUri: Uri? = null
        var currentBitmap: Bitmap? = null

        override fun getCurrentContentTitle(player: Player): CharSequence {

            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {

            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return mediaController.metadata.description.subtitle.toString()
        }


        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val iconUri = mediaController.metadata.description.iconUri
            return if (currentIconUri != iconUri || currentBitmap == null) {

                // Cache the bitmap for the current song so that successive calls to
                // `getCurrentLargeIcon` don't cause the bitmap to be recreated.
                currentIconUri = iconUri
                serviceScope.launch {
                    currentBitmap = iconUri?.let {
                        resolveUriAsBitmap(it)
                    }
                    currentBitmap?.let { callback.onBitmap(it) }
                }
                null
            } else {
                currentBitmap
            }
        }


        private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap? {
            return try {
                withContext(Dispatchers.IO) {
                    // Block on downloading artwork.
                    Glide.with(context).applyDefaultRequestOptions(glideOptions)
                        .asBitmap()
                        .load(uri)
                        .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
                        .get()
                }
            } catch (e : Exception) { null}

        }
    }
}

const val NOTIFICATION_LARGE_ICON_SIZE = 144 // px

private val glideOptions = RequestOptions()
    .fallback(R.drawable.ic_radio_default)
    .diskCacheStrategy(DiskCacheStrategy.DATA)