package com.example.radioplayer.ui.viewmodels

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.radioplayer.adapters.models.CountryWithRegion
import com.example.radioplayer.adapters.models.TagWithGenre
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.utils.COUNTRY_REGION_AFRICA
import com.example.radioplayer.utils.COUNTRY_REGION_ASIA
import com.example.radioplayer.utils.COUNTRY_REGION_CENTRAL_AMERICA
import com.example.radioplayer.utils.COUNTRY_REGION_EAST_EUROPE
import com.example.radioplayer.utils.COUNTRY_REGION_MIDDLE_EAST
import com.example.radioplayer.utils.COUNTRY_REGION_NORTH_AMERICA
import com.example.radioplayer.utils.COUNTRY_REGION_OCEANIA
import com.example.radioplayer.utils.COUNTRY_REGION_SOUTH_AMERICA
import com.example.radioplayer.utils.COUNTRY_REGION_THE_CARIBBEAN
import com.example.radioplayer.utils.COUNTRY_REGION_WEST_EUROPE
import com.example.radioplayer.utils.Language
import com.example.radioplayer.utils.TAG_BY_CLASSIC
import com.example.radioplayer.utils.TAG_BY_EXPERIMENTAL
import com.example.radioplayer.utils.TAG_BY_GENRE
import com.example.radioplayer.utils.TAG_BY_MINDFUL
import com.example.radioplayer.utils.TAG_BY_ORIGIN
import com.example.radioplayer.utils.TAG_BY_OTHER
import com.example.radioplayer.utils.TAG_BY_PERIOD
import com.example.radioplayer.utils.TAG_BY_RELIGION
import com.example.radioplayer.utils.TAG_BY_SPECIAL
import com.example.radioplayer.utils.TAG_BY_SUB_GENRE
import com.example.radioplayer.utils.TAG_BY_TALK
import com.example.radioplayer.utils.listOfAfrica
import com.example.radioplayer.utils.listOfAsia
import com.example.radioplayer.utils.listOfCentralAmerica
import com.example.radioplayer.utils.listOfEastEurope
import com.example.radioplayer.utils.listOfLanguages
import com.example.radioplayer.utils.listOfMiddleEast
import com.example.radioplayer.utils.listOfNorthAmerica
import com.example.radioplayer.utils.listOfOceania
import com.example.radioplayer.utils.listOfSouthAmerica
import com.example.radioplayer.utils.listOfTheCaribbean
import com.example.radioplayer.utils.listOfWestEurope
import com.example.radioplayer.utils.tagsListByGenre
import com.example.radioplayer.utils.tagsListByOrigin
import com.example.radioplayer.utils.tagsListByPeriod
import com.example.radioplayer.utils.tagsListBySubGenre
import com.example.radioplayer.utils.tagsListByTalk
import com.example.radioplayer.utils.tagsListClassics
import com.example.radioplayer.utils.tagsListExperimental
import com.example.radioplayer.utils.tagsListMindful
import com.example.radioplayer.utils.tagsListOther
import com.example.radioplayer.utils.tagsListReligion
import com.example.radioplayer.utils.tagsListSpecial
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.ArrayList
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SearchDialogsViewModel @Inject constructor(
    private val radioSource : RadioSource
) : ViewModel() {

    var tagAdapterPosition : Parcelable? = null
    var countriesAdapterPosition : Parcelable? = null


    private var currentLanguage = ""

    private var stationsCountForLanguage = 0

    fun updateLanguageCount(resultHandler : (Int) -> Unit) = viewModelScope.launch {

        val newLang = Locale.getDefault().language

        if(currentLanguage != newLang){

            var language : Language? = null

            for(i in listOfLanguages.indices){
                if(listOfLanguages[i].iso == newLang){
                    language = listOfLanguages[i]
                    break
                }
            }

            resultHandler(language?.stationCount ?: 0)
            stationsCountForLanguage

            language?.let {

                try {
                    val response = radioSource.getLanguages(language.name)
                    response.body()?.let { langs ->
                        var count = 0
                        langs.forEach {
                            count += it.stationcount
                        }
                        resultHandler(count)
                        stationsCountForLanguage = count
                        currentLanguage = newLang
                    }

                } catch (e : Exception){}
            }
        } else {
            resultHandler(stationsCountForLanguage)
        }
    }


    var isCountryListToUpdate = true


    fun updateCountryList(handleResults : () -> Unit) = viewModelScope.launch {

        if(isCountryListToUpdate){

            try {
                val countryList = radioSource.getAllCountries()
                if(countryList.isSuccessful){
                    countryList.body()?.let {
                        it.forEach { country ->

                            for(i in listOfCountries.indices){
                                if(listOfCountries[i] is CountryWithRegion.Country){
                                    if((listOfCountries[i] as CountryWithRegion.Country)
                                            .countryCode == country.iso_3166_1){
                                        (listOfCountries[i] as CountryWithRegion.Country).stationsCount =
                                            country.stationcount
                                        break
                                    }
                                }
                            }
                        }

                        handleResults()

                        isCountryListToUpdate = false
                    }
                }

            } catch (e : Exception){ }
        }
    }



    val tagsList by lazy { ArrayList<TagWithGenre>().apply {

        add(TagWithGenre.Genre(TAG_BY_PERIOD))
        addAll(tagsListByPeriod)

        add(TagWithGenre.Genre(TAG_BY_SPECIAL))
        addAll(tagsListSpecial)

        add(TagWithGenre.Genre(TAG_BY_GENRE))
        addAll(tagsListByGenre)

        add(TagWithGenre.Genre(TAG_BY_SUB_GENRE))
        addAll(tagsListBySubGenre)

        add(TagWithGenre.Genre(TAG_BY_CLASSIC))
        addAll(tagsListClassics)

        add(TagWithGenre.Genre(TAG_BY_MINDFUL))
        addAll(tagsListMindful)

        add(TagWithGenre.Genre(TAG_BY_EXPERIMENTAL))
        addAll(tagsListExperimental)

        add(TagWithGenre.Genre(TAG_BY_TALK))
        addAll(tagsListByTalk)

        add(TagWithGenre.Genre(TAG_BY_RELIGION))
        addAll(tagsListReligion)

        add(TagWithGenre.Genre(TAG_BY_ORIGIN))
        addAll(tagsListByOrigin)

        add(TagWithGenre.Genre(TAG_BY_OTHER))
        addAll(tagsListOther)
        }
    }

    val listOfCountries by lazy {
        ArrayList<CountryWithRegion>().apply {

            add(CountryWithRegion.Region(COUNTRY_REGION_AFRICA))
            addAll(listOfAfrica)
            add(CountryWithRegion.Region(COUNTRY_REGION_ASIA))
            addAll(listOfAsia)
            add(CountryWithRegion.Region(COUNTRY_REGION_CENTRAL_AMERICA))
            addAll(listOfCentralAmerica)
            add(CountryWithRegion.Region(COUNTRY_REGION_NORTH_AMERICA))
            addAll(listOfNorthAmerica)
            add(CountryWithRegion.Region(COUNTRY_REGION_SOUTH_AMERICA))
            addAll(listOfSouthAmerica)
            add(CountryWithRegion.Region(COUNTRY_REGION_EAST_EUROPE))
            addAll(listOfEastEurope)
            add(CountryWithRegion.Region(COUNTRY_REGION_WEST_EUROPE))
            addAll(listOfWestEurope)
            add(CountryWithRegion.Region(COUNTRY_REGION_MIDDLE_EAST))
            addAll(listOfMiddleEast)
            add(CountryWithRegion.Region(COUNTRY_REGION_OCEANIA))
            addAll(listOfOceania)
            add(CountryWithRegion.Region(COUNTRY_REGION_THE_CARIBBEAN))
            addAll(listOfTheCaribbean)

        }
    }



}