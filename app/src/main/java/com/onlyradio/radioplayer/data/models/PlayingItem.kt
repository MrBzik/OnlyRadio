package com.onlyradio.radioplayer.data.models

import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.data.local.entities.Recording

sealed class PlayingItem {

    class FromRadio(val radioStation: RadioStation) : PlayingItem()

    class FromRecordings(val recording: Recording) : PlayingItem()

}