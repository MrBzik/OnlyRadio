package com.example.radioplayer.data.models

import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Recording

sealed class PlayingItem {

    class FromRadio(val radioStation: RadioStation) : PlayingItem()

    class FromRecordings(val recording: Recording) : PlayingItem()

}