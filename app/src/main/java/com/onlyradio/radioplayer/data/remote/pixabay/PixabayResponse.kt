package com.onlyradio.radioplayer.data.remote.pixabay

data class PixabayResponse(
    val hits: List<Hit>,
    val total: Int,
    val totalHits: Int
)