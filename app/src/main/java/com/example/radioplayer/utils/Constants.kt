package com.example.radioplayer.utils

object Constants {

    const val BASE_RADIO_URL1 = "https://de1.api.radio-browser.info"

    const val BASE_RADIO_URL2 = "https://nl1.api.radio-browser.info/"

    const val BASE_RADIO_URL3 = "https://at1.api.radio-browser.info/"

    var BASE_RADIO_URL = "base radio url"


    val listOfUrls = listOf(BASE_RADIO_URL1, BASE_RADIO_URL2, BASE_RADIO_URL3)

    const val PIXABAY_BASE_URL = "https://pixabay.com"

    const val NOTIFICATION_ID = 1
    const val CHANNEL_ID = "radio"

    const val MEDIA_ROOT_ID = "media root id"

    const val NETWORK_ERROR = "network error"

    const val COMMAND_NEW_SEARCH = "new search"

    const val COMMAND_LOAD_FROM_PLAYLIST = "load from playlist"

    const val PAGE_SIZE = 10

    const val DATABASE_NAME = "radio_stations_db"


    const val SEARCH_FROM_API = 0
    const val SEARCH_FROM_FAVOURITES = 1
    const val SEARCH_FROM_PLAYLIST = 2
    const val SEARCH_FROM_HISTORY = 3


    const val DATE_FORMAT = "dd.MM.yyyy"


    const val HISTORY_OPTIONS = "History options"
    const val HISTORY_ONE_DAY = "one day"
    const val HISTORY_3_DATES = "3 dates"
    const val HISTORY_7_DATES = "7 dates"
    const val HISTORY_30_DATES = "30 dates"
    const val HISTORY_NEVER_CLEAN = "never clean"


    const val SEARCH_PREF_TAG = "search preferences tag"
    const val SEARCH_PREF_NAME = "search preferences name"
    const val SEARCH_PREF_COUNTRY = "search preferences country"
}