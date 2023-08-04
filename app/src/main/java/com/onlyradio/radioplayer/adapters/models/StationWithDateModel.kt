package com.onlyradio.radioplayer.adapters.models

import com.onlyradio.radioplayer.data.local.entities.RadioStation

sealed interface StationWithDateModel {

    class Station (val radioStation: RadioStation) : StationWithDateModel

    class DateSeparator (val date : String) : StationWithDateModel

    class DateSeparatorEnclosing (val date : String) : StationWithDateModel

}