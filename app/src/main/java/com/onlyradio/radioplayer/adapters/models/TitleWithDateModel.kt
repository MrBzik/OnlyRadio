package com.onlyradio.radioplayer.adapters.models

import com.onlyradio.radioplayer.data.local.entities.Title


sealed interface TitleWithDateModel {

    class TitleItem (val title: Title) : TitleWithDateModel

    class TitleDateSeparator (val date : String) : TitleWithDateModel

    class TitleDateSeparatorEnclosing (val date : String) : TitleWithDateModel

}