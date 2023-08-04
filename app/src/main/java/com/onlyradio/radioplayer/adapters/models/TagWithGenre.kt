package com.onlyradio.radioplayer.adapters.models

import kotlinx.serialization.Serializable




@Serializable
sealed class TagWithGenre {

    @Serializable
    class Tag(val tag : String,
            var stationCount : Int = 0,
            var stationCountExact : Int = 0
    ) : TagWithGenre()

    @Serializable
    class Genre (val genre : String) : TagWithGenre() {
       var isOpened = true
    }

    override fun equals(other: Any?): Boolean {
        return this is Tag && other is Tag && this.tag == other.tag
    }

    override fun hashCode(): Int {
        return if(this is Tag) tag.hashCode()
               else {
            (this as Genre).genre.hashCode()
        }
    }

}