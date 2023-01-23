package com.example.radioplayer.exoPlayer

import android.support.v4.media.MediaMetadataCompat
import com.example.radioplayer.data.local.entities.RadioStation

fun MediaMetadataCompat.toRadioStation() : RadioStation? {

   return description?.let {
        RadioStation(
            it.mediaId!!,
            it.iconUri.toString(),
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.description.toString()
        )
    }

}