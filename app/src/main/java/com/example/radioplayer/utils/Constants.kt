package com.example.radioplayer.utils

object Constants {

    const val BASE_RADIO_URL1 = "https://de1.api.radio-browser.info"

    const val BASE_RADIO_URL2 = "https://nl1.api.radio-browser.info"

    const val BASE_RADIO_URL3 = "https://at1.api.radio-browser.info"

    const val ADD_RADIO_STATION_URL = "https://www.radio-browser.info/add"

//    const val BASE_RADIO_URLTEST = "https://ts1.api.radio-browser.info"


    const val FRAG_SEARCH = 0
    const val FRAG_FAV = 1
    const val FRAG_HISTORY = 2
    const val FRAG_REC = 3
    const val FRAG_OPTIONS = 4


    const val API_RADIO_SEARCH_URL = "/json/stations/search"
    const val API_RADIO_TOP_VOTE_SEARCH_URL = "/json/stations/topvote"
    const val API_RADIO_ALL_COUNTRIES = "/json/countries"

    const val API_RADIO_LANGUAGES = "/json/languages/"

    const val BASE_RADIO_URL = "radio base url"


    val listOfUrls = listOf(BASE_RADIO_URL1, BASE_RADIO_URL2, BASE_RADIO_URL3)

    const val PIXABAY_BASE_URL = "https://pixabay.com"

    const val NOTIFICATION_ID = 1
    const val CHANNEL_ID = "radio"

//    const val RECORDING_CHANNEL_ID = "recording channel"
//    const val RECORDING_NOTIFICATION_ID = 7




    const val MEDIA_ROOT_ID = "media root id"

    const val NETWORK_ERROR = "network error"

    const val COMMAND_NEW_SEARCH = "new search"

    const val COMMAND_UPDATE_HISTORY = "command update history"

    const val COMMAND_START_RECORDING = "start exoRecord recording"
    const val COMMAND_STOP_RECORDING = "stop exoRecord recording"

    const val COMMAND_REMOVE_RECORDING_MEDIA_ITEM = "remove playing item from exoplayer"
    const val COMMAND_RESTORE_RECORDING_MEDIA_ITEM = "command restore rec media item"

    const val COMMAND_UPDATE_REC_PLAYBACK_SPEED = "update recordings playback speed"
    const val COMMAND_UPDATE_RADIO_PLAYBACK_SPEED = "update radio playback speed"
    const val COMMAND_UPDATE_RADIO_PLAYBACK_PITCH= "update radio playback pitch"

    const val COMMAND_RESTART_PLAYER = "command to recreate player"

    const val COMMAND_COMPARE_DATES_PREF_AND_CLEAN = "command check date and update history"

    const val COMMAND_UPDATE_FAV_PLAYLIST = "command update fav playlist"

    const val COMMAND_UPDATE_HISTORY_MEDIA_ITEMS = "command update history mediaitems"
    const val IS_TO_CLEAR_HISTORY_ITEMS = "is to clear mediaItems"

    const val COMMAND_UPDATE_HISTORY_ONE_DATE_MEDIA_ITEMS = "command update history one date media items"


    const val COMMAND_CLEAR_MEDIA_ITEMS = "command clear media items"

//    const val COMMAND_CHANGE_MEDIA_ITEMS = "command change media items"

    const val COMMAND_REMOVE_MEDIA_ITEM = "command remove media item"
    const val COMMAND_ADD_MEDIA_ITEM = "command add media item"

    const val COMMAND_ON_DROP_STATION_IN_PLAYLIST = "command on drop station in playlist"



    const val COMMAND_PAUSE_PLAYER = "command pause player"
    const val COMMAND_START_PLAYER = "command start player"

    const val COMMAND_STOP_SERVICE = "command to stop service"



    const val PAGE_SIZE = 10

    const val DATABASE_NAME = "radio_stations_db"

    const val SEARCH_FLAG = "search flag"

    const val IS_NEW_SEARCH = "is new search"

    const val NO_ITEMS = -2
    const val NO_PLAYLIST = -1
    const val SEARCH_FROM_API = 0
    const val SEARCH_FROM_FAVOURITES = 1
    const val SEARCH_FROM_PLAYLIST = 2
    const val SEARCH_FROM_HISTORY = 3
    const val SEARCH_FROM_HISTORY_ONE_DATE = 4
    const val SEARCH_FROM_RECORDINGS = 5
    const val PLAY_WHEN_READY = "play when ready"
    const val ITEM_INDEX = "index of item"
    const val ITEM_ID = "id of item"

    const val IS_CHANGE_MEDIA_ITEMS = "id of station from history"

    const val TITLE_UNKNOWN = "Playing: no info"

    // History

    const val HISTORY_DATES_PREF_DEFAULT = 3

    const val HISTORY_PREF = "history pref"

    const val HISTORY_PREF_DATES = "preference for history dates"

    const val HISTORY_PREF_BOOKMARK = "pref for bookmarked titles"

    const val HISTORY_BOOKMARK_PREF_DEFAULT = 20


    const val FULL_DATE_FORMAT = "d 'of' MMMM', 'yyyy"

    const val SHORT_DATE_FORMAT = "MMM, ', ' d"

    const val TEXT_SIZE_STATION_TITLE_PREF = "pref for text size of stations titles"






// Search preferences

    const val SEARCH_PREF_TAG = "search preferences tag"
    const val SEARCH_PREF_NAME = "search preferences name"
    const val SEARCH_PREF_NAME_AUTO = "search pref name auto search"
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
    const val REC_QUALITY_LOW = 0.0001f
    const val REC_QUALITY_MEDIUM = 0.2f
    const val REC_QUALITY_DEF = 0.4f
    const val REC_QUALITY_HIGH = 0.6f
    const val REC_QUALITY_ULTRA = 0.8f
    const val REC_QUALITY_MAX = 0.9f



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