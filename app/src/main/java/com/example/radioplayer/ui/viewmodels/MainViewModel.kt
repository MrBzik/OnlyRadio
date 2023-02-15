package com.example.radioplayer.ui.viewmodels

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import androidx.paging.*
import com.example.radioplayer.adapters.datasources.RadioStationsDataSource
import com.example.radioplayer.adapters.datasources.StationsPageLoader
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.exoPlayer.*
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.COMMAND_NEW_SEARCH
import com.example.radioplayer.utils.Constants.PAGE_SIZE
import com.example.radioplayer.utils.Constants.SEARCH_PREF_COUNTRY
import com.example.radioplayer.utils.Constants.SEARCH_PREF_NAME
import com.example.radioplayer.utils.Constants.SEARCH_PREF_TAG
import com.example.radioplayer.utils.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@HiltViewModel
class MainViewModel @Inject constructor(
    app : Application,
    private val radioServiceConnection: RadioServiceConnection,
    private val radioSource: RadioSource,
    private val repository: DatabaseRepository
) : AndroidViewModel(app) {

       val isConnected = radioServiceConnection.isConnected
       val currentRadioStation = radioServiceConnection.currentRadioStation
       val networkError = radioServiceConnection.networkError
       val playbackState = radioServiceConnection.playbackState
       private var listOfStations = listOf<RadioStation>()
       var isNewSearch = true
       var isDelayNeededForServiceConnection = true
       val newRadioStation : MutableLiveData<RadioStation> = MutableLiveData()

       val searchParamTag : MutableLiveData<String> = MutableLiveData()
       val searchParamName : MutableLiveData<String> = MutableLiveData()
       val searchParamCountry : MutableLiveData<String> = MutableLiveData()

       val searchPreferences = app.getSharedPreferences("SearchPref", Context.MODE_PRIVATE)



        init {

            currentRadioStation.value?.let {

                viewModelScope.launch {
                  val currentStation = repository.getCurrentRadioStation(
                      it.getString(METADATA_KEY_MEDIA_ID)
                  )
                    newRadioStation.postValue(currentStation)

                }

            }

        }

       private suspend fun searchWithNewParams(
            limit : Int, offset : Int, bundle: Bundle
        ) : List<RadioStation> {

           withContext(Dispatchers.IO) {

               val calcOffset = limit * offset

               bundle.apply {

                   val tag = getString("TAG") ?: ""
                   val name = getString("NAME") ?: ""
                   val country = getString("COUNTRY") ?: ""

                   this.putInt("OFFSET", calcOffset)

                   val response = radioSource.getRadioStationsSource(
                       offset = calcOffset,
                       pageSize = limit,
                       country = country,
                       tag = tag,
                       name = name
                   )

                   response?.let {

                       listOfStations = it.map { station ->

                           RadioStation(
                               favicon = station.favicon,
                               name = station.name,
                               stationuuid = station.stationuuid,
                               country = station.country,
                               url = station.url_resolved,
                               homepage = station.homepage,
                               tags = station.tags,
                               language = station.language,
                               inPlaylists = 0,
                               isFavoured = false
                           )
                       }
                   }
               }

                if(isDelayNeededForServiceConnection){
                    delay(1000)
                    isDelayNeededForServiceConnection = false
                }
       }

           val firstRunBundle = Bundle().apply {

             this.putBoolean("IS_NEW_SEARCH", isNewSearch)

           }

           radioServiceConnection.sendCommand(COMMAND_NEW_SEARCH, firstRunBundle)

           isNewSearch = false

           return listOfStations

        }



    private fun searchStationsPaging(
        bundle: Bundle
    ): Flow<PagingData<RadioStation>> {
        val loader : StationsPageLoader = { pageIndex, pageSize ->
            searchWithNewParams(pageSize, pageIndex, bundle)
        }

        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                RadioStationsDataSource(loader, PAGE_SIZE)
            }
        ).flow
    }



    private val initialSearchBundle = Bundle().apply {

        val tag = searchPreferences.getString(SEARCH_PREF_TAG, "")
        val name = searchPreferences.getString(SEARCH_PREF_NAME, "")
        val country = searchPreferences.getString(SEARCH_PREF_COUNTRY, "")

        searchParamTag.postValue(tag ?: "")
        searchParamName.postValue(name ?: "")
        searchParamCountry.postValue(country ?: "")

        putString("TAG", tag)
        putString("NAME", name)
        putString("COUNTRY", country)

    }


    private val searchBy = MutableLiveData(initialSearchBundle)

    val stationsFlow = searchBy.asFlow()
        .flatMapLatest {
            searchStationsPaging(it)
        }
        .cachedIn(viewModelScope)


    fun setSearchBy(value : Bundle){

        searchBy.value?.let {
        if(it.getString("TAG") == value.getString("TAG")
            && it.getString("NAME") == value.getString("NAME")
            && it.getString("COUNTRY") == value.getString("COUNTRY")

        )  return
                searchBy.value = value
        }

    }




        fun playOrToggleStation(station : RadioStation, searchFlag : Int = 0) {

            val isPrepared = playbackState.value?.isPrepared ?: false

            if(isPrepared && station.stationuuid
                    == currentRadioStation.value?.getString(METADATA_KEY_MEDIA_ID)){
                playbackState.value?.let { playbackState ->
                    when {
                        playbackState.isPlaying -> radioServiceConnection.transportControls.pause()
                        playbackState.isPlayEnabled -> radioServiceConnection.transportControls.play()

                    }
                }
            } else{
                radioServiceConnection.transportControls
                    .playFromMediaId(station.stationuuid, bundleOf(Pair("SEARCH_FLAG", searchFlag)))
            }
        }


//    private fun getCountries() = viewModelScope.launch {
//
//        val response = radioSource.getAllCountries()
//
//        var count = 0
//
//        val listOfCountries = mutableListOf<Country>()
//
//        val builder = StringBuilder()
//        val builder2 = StringBuilder()
//        val builder3 = StringBuilder()
//        val builder4 = StringBuilder()
//        val builder5 = StringBuilder()
//
//
//        response.forEach {
//
//            if(count < 45) {
//                count++
//                builder.append(
//                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
//                )
//            } else if(count < 90) {
//            count++
//            builder2.append(
//                "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
//            )
//        } else if(count < 135) {
//                count++
//                builder3.append(
//                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
//                )
//            } else if(count < 175) {
//                count++
//                builder4.append(
//                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
//                )
//            } else {
//                count++
//                builder5.append(
//                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
//                )
//            }
//
//        }
//
//        Log.d("CHECKTAGS", builder.toString())
//        Log.d("CHECKTAGS", builder2.toString())
//        Log.d("CHECKTAGS", builder3.toString())
//        Log.d("CHECKTAGS", builder4.toString())
//        Log.d("CHECKTAGS", builder5.toString())
//
//    }


//    init {
//
//        viewModelScope.launch {
//           val list = repository.getAllStations()
//            Log.d("CHECKTAGS", list.toString())
//        }
//
//    }

}

