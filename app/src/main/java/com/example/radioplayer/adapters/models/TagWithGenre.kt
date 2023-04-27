package com.example.radioplayer.adapters.models

sealed class TagWithGenre {

    class Tag(val tag : String,
            var stationCount : Int = 0,
            var stationCountExact : Int = 0
    ) : TagWithGenre()

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