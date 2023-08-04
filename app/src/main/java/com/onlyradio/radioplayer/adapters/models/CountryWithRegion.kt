package com.onlyradio.radioplayer.adapters.models

sealed class CountryWithRegion {

    class Country(
        val countryName : String,
        val countryCode : String,
        var stationsCount : Int = 0
    ) : CountryWithRegion()

    class Region (val region : String) : CountryWithRegion() {
        var isOpened = true
    }

    override fun equals(other: Any?): Boolean {
        return this is Country && other is Country && this.countryCode == this.countryCode
    }

    override fun hashCode(): Int {
        return if(this is Country) countryCode.hashCode()
                else (this as Region).region.hashCode()

    }



}
