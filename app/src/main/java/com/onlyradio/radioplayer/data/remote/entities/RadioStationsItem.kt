package com.onlyradio.radioplayer.data.remote.entities

data class RadioStationsItem(
    val bitrate: Int,
    val changeuuid: String,
    val clickcount: Int,
    val clicktimestamp: String,
    val clicktimestamp_iso8601: String,
    val clicktrend: Int,
    val codec: String,
    val country: String,
    val countrycode: String,
    val favicon: String,
    val geo_lat: Double,
    val geo_long: Double,
    val has_extended_info: Boolean,
    val hls: Int,
    val homepage: String,
    val iso_3166_2: String,
    val language: String,
    val languagecodes: String,
    val lastchangetime: String,
    val lastchangetime_iso8601: String,
    val lastcheckok: Int,
    val lastcheckoktime: String,
    val lastcheckoktime_iso8601: String,
    val lastchecktime: String,
    val lastchecktime_iso8601: String,
    val lastlocalchecktime: String,
    val lastlocalchecktime_iso8601: String,
    val name: String,
    val serveruuid: String,
    val ssl_error: Int,
    val state: String,
    val stationuuid: String,
    val tags: String,
    val url: String,
    val url_resolved: String,
    val votes: Int
)