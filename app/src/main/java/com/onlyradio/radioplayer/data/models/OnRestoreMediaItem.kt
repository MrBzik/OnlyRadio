package com.onlyradio.radioplayer.data.models

data class OnRestoreMediaItem(
    val index : Int,
    val playlist : Int,
    val playlistName : String,
    val stationId : String,
    var timeOfInsertion : Long = 0
)
