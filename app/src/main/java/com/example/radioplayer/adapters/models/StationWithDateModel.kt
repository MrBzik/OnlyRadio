package com.example.radioplayer.adapters.models

import com.example.radioplayer.data.local.entities.RadioStation

sealed class StationWithDateModel {

    class Station (val radioStation: RadioStation) : StationWithDateModel()

    class DateSeparator (val date : String) : StationWithDateModel()

    class DateSeparatorEnclosing (val date : String) : StationWithDateModel()

}