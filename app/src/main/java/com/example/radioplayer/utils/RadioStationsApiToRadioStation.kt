package com.example.radioplayer.utils

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.remote.entities.RadioStationsItem

fun RadioStationsItem.toRadioStation() : RadioStation{
    val country = when (countrycode) {
        "US" -> "USA"
        "GB" -> "UK"
        "RU" -> "Russia"
        else -> country
    }


   return RadioStation(
        favicon = favicon,
        name = name,
        stationuuid = stationuuid,
        country = country,
        url = url_resolved,
        homepage = homepage,
        tags = tags,
        language = language,
        favouredAt = 0,
        state = state,
        bitrate = bitrate,
       lastClick = 0,
       playDuration = 0,
       inPlaylistsCount = 0
    )

}

fun RadioStation.toMediaMetadataCompat( ): MediaMetadataCompat = MediaMetadataCompat.Builder()
        .putString(METADATA_KEY_TITLE, name)
        .putString(METADATA_KEY_DISPLAY_TITLE, name)
        .putString(METADATA_KEY_MEDIA_ID, stationuuid)
        .putString(METADATA_KEY_ALBUM_ART_URI, favicon)
        .putString(METADATA_KEY_DISPLAY_ICON_URI, favicon)
        .putString(METADATA_KEY_MEDIA_URI, url)
        .putString(METADATA_KEY_DISPLAY_SUBTITLE, country)
        .build()