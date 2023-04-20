package com.example.radioplayer.utils

object Constants {

    const val BASE_RADIO_URL1 = "https://de1.api.radio-browser.info"

    const val BASE_RADIO_URL2 = "https://nl1.api.radio-browser.info"

    const val BASE_RADIO_URL3 = "https://at1.api.radio-browser.info"

//    const val BASE_RADIO_URLTEST = "https://ts1.api.radio-browser.info"

    const val API_RADIO_SEARCH_URL = "/json/stations/search"
    const val API_RADIO_TOP_VOTE_SEARCH_URL = "/json/stations/topvote"

    const val BASE_RADIO_URL = "radio base url"


    val listOfUrls = listOf(BASE_RADIO_URL1, BASE_RADIO_URL2, BASE_RADIO_URL3)

    const val PIXABAY_BASE_URL = "https://pixabay.com"

    const val NOTIFICATION_ID = 1
    const val CHANNEL_ID = "radio"

    const val RECORDING_CHANNEL_ID = "recording channel"
    const val RECORDING_NOTIFICATION_ID = 7



    const val MEDIA_ROOT_ID = "media root id"

    const val NETWORK_ERROR = "network error"

    const val COMMAND_NEW_SEARCH = "new search"
    const val COMMAND_START_RECORDING = "start exoRecord recording"
    const val COMMAND_STOP_RECORDING = "stop exoRecord recording"
    const val COMMAND_REMOVE_CURRENT_PLAYING_ITEM = "remove playing item from exoplayer"
    const val COMMAND_UPDATE_REC_PLAYBACK_SPEED = "update recordings playback speed"
    const val COMMAND_UPDATE_RADIO_PLAYBACK_SPEED = "update radio playback speed"
    const val COMMAND_UPDATE_RADIO_PLAYBACK_PITCH= "update radio playback pitch"

    const val COMMAND_RESTART_PLAYER = "command to recreate player"

    const val COMMAND_PAUSE_PLAYER = "command pause player"
    const val COMMAND_START_PLAYER = "command start player"



    const val PAGE_SIZE = 10

    const val DATABASE_NAME = "radio_stations_db"

    const val SEARCH_FLAG = "search flag"
    const val SEARCH_FROM_API = 0
    const val SEARCH_FROM_FAVOURITES = 1
    const val SEARCH_FROM_PLAYLIST = 2
    const val SEARCH_FROM_HISTORY = 3
    const val SEARCH_FROM_RECORDINGS = 4
    const val PLAY_WHEN_READY = "play when ready"

    const val DATE_FORMAT = "dd.MM.yyyy HH:mm"


    const val HISTORY_OPTIONS = "History options"
    const val HISTORY_ONE_DAY = 1
    const val HISTORY_3_DATES = 3
    const val HISTORY_7_DATES = 7
    const val HISTORY_15_DATES = 15
    const val HISTORY_21_DATES = 21
    const val HISTORY_30_DATES = 30


// Search preferences

    const val SEARCH_PREF_TAG = "search preferences tag"
    const val SEARCH_PREF_NAME = "search preferences name"
    const val SEARCH_PREF_COUNTRY = "search preferences country"
    const val SEARCH_FULL_COUNTRY_NAME = "full country name for no result message"
    const val SEARCH_PREF_ORDER = "search preference for order"
    const val SEARCH_PREF_MIN_BIT = "search pref for minimum bitrate"
    const val SEARCH_PREF_MAX_BIT = "search pref for maximum bitrate"
    const val IS_TAG_EXACT = "is tag exact"
    const val IS_NAME_EXACT = "is name exact"
    const val IS_SEARCH_FILTER_LANGUAGE = "is to filter by system language"


    // SearchFragment Floating button positioning
    const val FAB_POSITION_X = "FAB_POSITION_X"
    const val FAB_POSITION_Y = "FAB_POSITION_Y"
    const val IS_FAB_UPDATED = "IS_FAB_UPDATED"
    const val SEARCH_BTN_PREF = "search button position pref"

    //Recording preferences

    const val RECORDING_QUALITY_PREF = "recording quality pref"

    // DarkMode pref

    const val DARK_MODE_PREF = "dark mode on or off"

    const val RECONNECT_PREF = "reconnect on lost connection"

    const val FOREGROUND_PREF = "pref for foreground on closing app"


    // Buffer pref

    const val BUFFER_PREF = "buffer preferences"

    const val BUFFER_SIZE_IN_MILLS = "buffer size in mills pref"

    const val BUFFER_SIZE_IN_BYTES = "buffer size in bytes pref"
    const val IS_TO_SET_BUFFER_IN_BYTES = "is to set target buffer in bytes"

    const val BUFFER_FOR_PLAYBACK = "buffer size to start playback"

    const val IS_ADAPTIVE_LOADER_TO_USE = "is to use adaptive loader"


    // Audio effects

    const val COMMAND_CHANGE_REVERB_MODE = "change reverb mode"

    const val COMMAND_CHANGE_BASS_LEVEL = "change bass boost level"


}