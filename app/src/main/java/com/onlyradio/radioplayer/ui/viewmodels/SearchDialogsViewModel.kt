package com.onlyradio.radioplayer.ui.viewmodels

import android.app.Application
import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.onlyradio.radioplayer.adapters.models.CountryWithRegion
import com.onlyradio.radioplayer.adapters.models.serializable.TagWithGenre
import com.onlyradio.radioplayer.exoPlayer.RadioSource
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_AFRICA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_ASIA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_CENTRAL_AMERICA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_EAST_EUROPE
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_MIDDLE_EAST
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_NORTH_AMERICA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_OCEANIA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_SOUTH_AMERICA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_THE_CARIBBEAN
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_WEST_EUROPE
import com.onlyradio.radioplayer.utils.Language
import com.onlyradio.radioplayer.utils.TAG_BY_CLASSIC
import com.onlyradio.radioplayer.utils.TAG_BY_EXPERIMENTAL
import com.onlyradio.radioplayer.utils.TAG_BY_GENRE
import com.onlyradio.radioplayer.utils.TAG_BY_MINDFUL
import com.onlyradio.radioplayer.utils.TAG_BY_ORIGIN
import com.onlyradio.radioplayer.utils.TAG_BY_OTHER
import com.onlyradio.radioplayer.utils.TAG_BY_PERIOD
import com.onlyradio.radioplayer.utils.TAG_BY_RELIGION
import com.onlyradio.radioplayer.utils.TAG_BY_SPECIAL
import com.onlyradio.radioplayer.utils.TAG_BY_SUB_GENRE
import com.onlyradio.radioplayer.utils.TAG_BY_TALK
import com.onlyradio.radioplayer.utils.listOfAfrica
import com.onlyradio.radioplayer.utils.listOfAsia
import com.onlyradio.radioplayer.utils.listOfCentralAmerica
import com.onlyradio.radioplayer.utils.listOfEastEurope
import com.onlyradio.radioplayer.utils.listOfLanguages
import com.onlyradio.radioplayer.utils.listOfMiddleEast
import com.onlyradio.radioplayer.utils.listOfNorthAmerica
import com.onlyradio.radioplayer.utils.listOfOceania
import com.onlyradio.radioplayer.utils.listOfSouthAmerica
import com.onlyradio.radioplayer.utils.listOfTheCaribbean
import com.onlyradio.radioplayer.utils.listOfWestEurope
import com.onlyradio.radioplayer.utils.tagsListByGenre
import com.onlyradio.radioplayer.utils.tagsListByOrigin
import com.onlyradio.radioplayer.utils.tagsListByPeriod
import com.onlyradio.radioplayer.utils.tagsListBySubGenre
import com.onlyradio.radioplayer.utils.tagsListByTalk
import com.onlyradio.radioplayer.utils.tagsListClassics
import com.onlyradio.radioplayer.utils.tagsListExperimental
import com.onlyradio.radioplayer.utils.tagsListMindful
import com.onlyradio.radioplayer.utils.tagsListOther
import com.onlyradio.radioplayer.utils.tagsListReligion
import com.onlyradio.radioplayer.utils.tagsListSpecial
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val TAGS_FILE_NAME = "allTagsFile.txt"
const val TAGS_UPDATE_PREF = "tags update pref"
const val TAGS_EXCLUDED_FILE_NAME = "excluded_tags.txt"

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
         response?.body()?.let {tagsResponse ->

             val excludedTagsSet = try {
                 val inputStream: InputStream = app.assets.open("excluded_tags/excluded_tags.txt")
                 val inputString = inputStream.bufferedReader().use{it.readText()}
                 Json.decodeFromString<HashSet<String>>(inputString)
             } catch (e : Exception){
                 HashSet()
             }

             val list = listOf(
                 tagsListByPeriod.toList(), tagsListSpecial.toList(), tagsListByGenre.toList(),
                 tagsListBySubGenre.toList(), tagsListClassics.toList(), tagsListMindful.toList(),
                 tagsListExperimental.toList(), tagsListByTalk.toList(), tagsListReligion.toList(),
                 tagsListByOrigin.toList(), tagsListOther.toList()
             )


             tagsResponse.forEach {tagItem ->

                 if(!excludedTagsSet.contains(tagItem.name)){

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
             }


             val file = File(app.filesDir.absolutePath + File.separator + TAGS_FILE_NAME)

             val json = Json.encodeToString(list)

             file.writeText(json)

             tagsPref.edit().putLong(TAGS_UPDATE_PREF, System.currentTimeMillis()).apply()

         } ?: run { isToCheckTags = true }

//         Log.d("CHECKTAGS", "took time : $time")

     }



//    fun generateExcludedTags() = viewModelScope.launch{
//        val response = radioSource.getAllTags()
//
//        val excludedSet = HashSet<String>()
//        val listOfTags = initiateTagsList()
//        Log.d("CHECKTAGS", "response is : ${response?.body()?.size}, and included tags - ${listOfTags.size}")
//
//        response?.body()?.forEach { tagItem ->
//
//            var included = false
//
//            listOfTags.forEach {tagWithGenre ->
//
//                if(tagWithGenre is TagWithGenre.Tag){
//
//                    if(tagItem.name.contains(tagWithGenre.tag)){
//                        included = true
//                    }
//                }
//            }
//
//            if(!included)
//                excludedSet.add(tagItem.name)
//
//        }
//
//        Log.d("CHECKTAGS", "set in the end : ${excludedSet.size}")
//
//        val file = File(app.filesDir.absolutePath + File.separator + TAGS_EXCLUDED_FILE_NAME)
//
//        val json = Json.encodeToString(excludedSet)
//
//        file.writeText(json)
//
//    }



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


//        val inputStream: InputStream = app.assets.open("allTagsFile.txt")
//        val inputString = inputStream.bufferedReader().use{it.readText()}

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