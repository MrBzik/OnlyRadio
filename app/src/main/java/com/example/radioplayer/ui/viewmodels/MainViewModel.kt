package com.example.radioplayer.ui.viewmodels

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import androidx.paging.*
import com.example.radioplayer.adapters.PagingRadioAdapter
import com.example.radioplayer.adapters.datasources.RadioStationsDataSource
import com.example.radioplayer.adapters.datasources.StationsPageLoader
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.remote.entities.RadioTagsItem
import com.example.radioplayer.exoPlayer.*
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.utils.Constants.COMMAND_NEW_SEARCH
import com.example.radioplayer.utils.Constants.PAGE_SIZE
import com.example.radioplayer.utils.Country
import com.hbb20.countrypicker.models.CPCountry
import com.hbb20.countrypicker.view.CPViewHelper
import com.hbb20.countrypicker.view.prepareCustomCountryPickerView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import java.io.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@HiltViewModel
class MainViewModel @Inject constructor(
    private val radioServiceConnection: RadioServiceConnection,
    private val radioSource: RadioSource,
    private val repository: DatabaseRepository
) : ViewModel() {

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
                   val isTopSearch = getBoolean("SEARCH_TOP")

                   this.putInt("OFFSET", calcOffset)

                   val response = radioSource.getRadioStationsSource(
                       offset = calcOffset,
                       pageSize = limit,
                       isTopSearch = isTopSearch,
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



    private val searchBy = MutableLiveData(bundleOf(Pair("SEARCH_TOP", true)))

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
            && it.getBoolean("SEARCH_TOP") == value.getBoolean("SEARCH_TOP")
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


    private fun getCountries() = viewModelScope.launch {

        val response = radioSource.getAllCountries().body()

        var count = 0

        val listOfCountries = mutableListOf<Country>()

        val builder = StringBuilder()
        val builder2 = StringBuilder()
        val builder3 = StringBuilder()
        val builder4 = StringBuilder()
        val builder5 = StringBuilder()


        response?.forEach {

            if(count < 45) {
                count++
                builder.append(
                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
                )
            } else if(count < 90) {
            count++
            builder2.append(
                "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
            )
        } else if(count < 135) {
                count++
                builder3.append(
                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
                )
            } else if(count < 175) {
                count++
                builder4.append(
                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
                )
            } else {
                count++
                builder5.append(
                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
                )
            }

        }

        Log.d("CHECKTAGS", builder.toString())
        Log.d("CHECKTAGS", builder2.toString())
        Log.d("CHECKTAGS", builder3.toString())
        Log.d("CHECKTAGS", builder4.toString())
        Log.d("CHECKTAGS", builder5.toString())

    }

//    init {
//        getCountries()
//    }
}

