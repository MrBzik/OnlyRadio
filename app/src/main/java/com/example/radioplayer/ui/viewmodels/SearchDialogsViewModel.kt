package com.example.radioplayer.ui.viewmodels

import android.app.Application
import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.util.ArrayList
import java.util.Calendar
import java.util.Date

import java.util.Locale
import java.util.TreeMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.LinkedHashMap
import kotlin.system.measureTimeMillis

const val TAGS_FILE_NAME = "allTagsFile.txt"
const val TAGS_UPDATE_PREF = "tags update pref"

@HiltViewModel
class SearchDialogsViewModel @Inject constructor(
    private val radioSource : RadioSource,
    private val app : Application
) : AndroidViewModel(app) {

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

    private val tagsPref by lazy {
        app.getSharedPreferences(TAGS_UPDATE_PREF, Context.MODE_PRIVATE)
    }


    var isToCheckTags = true
    fun checkTagsLastUpdate(){

        if(isToCheckTags){
            isToCheckTags = false
            val lastUpdateMills = tagsPref.getLong(TAGS_UPDATE_PREF, 0L)
            val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
            val lastUpdateDay = TimeUnit.MILLISECONDS.toDays(lastUpdateMills)

            if(today - lastUpdateDay > 5)
                calculateTagsCounts()
        }
    }

     private fun calculateTagsCounts() = viewModelScope.launch(Dispatchers.IO) {

        val response = try {
            radioSource.getAllTags()
        } catch (e: Exception){
            null
        }

         response?.body()?.let {

             val list = listOf(
                 tagsListByPeriod.toList(), tagsListSpecial.toList(), tagsListByGenre.toList(),
                 tagsListBySubGenre.toList(), tagsListClassics.toList(), tagsListMindful.toList(),
                 tagsListExperimental.toList(), tagsListByTalk.toList(), tagsListReligion.toList(),
                 tagsListByOrigin.toList(), tagsListOther.toList()
             )

             it.forEach {tagItem ->

                 list.forEachIndexed { listIndex, sublist ->

                     sublist.forEachIndexed { index, tagWithGenre ->

                         if(tagItem.name.contains(tagWithGenre.tag)){
                             list[listIndex][index].stationCount += tagItem.stationcount
                             if(tagWithGenre.tag == tagItem.name){
                                 list[listIndex][index].stationCountExact = tagItem.stationcount
                             }
                         }
                     }
                 }
             }


             val file = File(app.filesDir.absolutePath + File.separator + TAGS_FILE_NAME)

             val json = Json.encodeToString(list)

             file.writeText(json)

             tagsPref.edit().putLong(TAGS_UPDATE_PREF, System.currentTimeMillis()).apply()

         } ?: run { isToCheckTags = true }
     }


    val tagsListFlow by lazy {
        MutableStateFlow(initiateTagsList())
    }

    fun updateTagsFlow(genre : TagWithGenre.Genre, position : Int){

        tagsListFlow.update {

            val list = getList(genre.genre)
            if(genre.isOpened) it.removeAll(list)
            else it.addAll(position+1, list)
            (it[position] as TagWithGenre.Genre).isOpened = !genre.isOpened
            it.clone() as ArrayList<TagWithGenre>
        }
    }


    private fun getList(genre : String) : Set<TagWithGenre> {
        return when(genre){
            TAG_BY_PERIOD -> tagsListByPeriod

            TAG_BY_GENRE -> tagsListByGenre

            TAG_BY_SUB_GENRE -> tagsListBySubGenre

            TAG_BY_MINDFUL -> tagsListMindful

            TAG_BY_CLASSIC -> tagsListClassics

            TAG_BY_EXPERIMENTAL -> tagsListExperimental

            TAG_BY_SPECIAL -> tagsListSpecial

            TAG_BY_TALK -> tagsListByTalk

            TAG_BY_RELIGION -> tagsListReligion

            TAG_BY_ORIGIN -> tagsListByOrigin

            else -> tagsListOther
        }
    }


    private fun initiateTagsList() : ArrayList<TagWithGenre>{

        val file = File(app.filesDir.absolutePath + File.separator + TAGS_FILE_NAME)
        val isExists = file.exists()

        return ArrayList<TagWithGenre>().apply {

            if(isExists) {
                val json = file.readText()
                val list = Json.decodeFromString<List<List<TagWithGenre.Tag>>>(json)
                updateTagsLists(list)
            }

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




    private fun updateTagsLists(list : List<List<TagWithGenre.Tag>>){
        tagsListByPeriod = list[0].toSet()
        tagsListSpecial = list[1].toSet()
        tagsListByGenre = list[2].toSet()
        tagsListBySubGenre = list[3].toSet()
        tagsListClassics = list[4].toSet()
        tagsListMindful = list[5].toSet()
        tagsListExperimental = list[6].toSet()
        tagsListByTalk = list[7].toSet()
        tagsListReligion = list[8].toSet()
        tagsListByOrigin = list[9].toSet()
        tagsListOther = list[10].toSet()
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