package com.example.radioplayer.adapters.models

import com.example.radioplayer.data.local.entities.Title


sealed interface TitleWithDateModel {

    class TitleItem (val title: Title) : TitleWithDateModel

    class TitleDateSeparator (val date : String) : TitleWithDateModel

    class TitleDateSeparatorEnclosing (val date : String) : TitleWithDateModel

}