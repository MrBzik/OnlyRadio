package com.example.radioplayer.ui.viewmodels

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import androidx.paging.*
import com.example.radioplayer.adapters.RadioStationsDataSource
import com.example.radioplayer.adapters.StationsPageLoader
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.remote.entities.RadioTagsItem
import com.example.radioplayer.exoPlayer.*
import com.example.radioplayer.utils.Constants.COMMAND_NEW_SEARCH
import com.example.radioplayer.utils.Constants.PAGE_SIZE
import com.hbb20.countrypicker.models.CPCountry
import com.hbb20.countrypicker.view.CPViewHelper
import com.hbb20.countrypicker.view.prepareCustomCountryPickerView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@HiltViewModel
class MainViewModel @Inject constructor(
       private val radioServiceConnection: RadioServiceConnection,
       private val radioSource: RadioSource,
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
                               language = station.language
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




        fun playOrToggleStation(station : RadioStation, toggle : Boolean = false) {

            val isPrepared = playbackState.value?.isPrepared ?: false

            if(isPrepared && station.stationuuid
                    == currentRadioStation.value?.getString(METADATA_KEY_MEDIA_ID)){
                playbackState.value?.let { playbackState ->
                    when {
                        playbackState.isPlaying -> if(toggle) radioServiceConnection.transportControls.pause()
                        playbackState.isPlayEnabled -> radioServiceConnection.transportControls.play()

                    }
                }
            } else{
                radioServiceConnection.transportControls.playFromMediaId(station.stationuuid, null)
            }
        }





    private fun getAllCountries () = viewModelScope.launch {

        val response = radioSource.getAllCountries()

        val result : LinkedHashMap<String, String> = LinkedHashMap()


        result["Andorra"] = "AD"

        response?.forEach {


        }

        Log.d("CHECKTAGS", result.toString())

    }




    private fun getAllTags () = viewModelScope.launch {

        val response = radioSource.getAllTags()

        var included = 0
        var excluded = 0

        response?.let{

            it.forEach{
                included += it.stationcount
            }


            Log.d("CHECKTAGS", "initial size : ${it.size}")

          val withoutMinors =  it.filter {
                it.stationcount > 10
            }

            Log.d("CHECKTAGS", "minus minors : ${withoutMinors.size}")


            val withoutObvious = mutableListOf<RadioTagsItem>()

                withoutMinors.forEach { item ->

                    if(

                        item.name.contains("radio") ||
                        item.name.contains("music") ||
                        item.name.contains("estación") ||
                        item.name.contains("fm") ||
                        item.name.contains("música") ||
                        item.name.contains("variety") ||
                        item.name.contains("various") ||



                        item.name.contains("hits") ||
                        item.name.contains("top") ||
                        item.name.contains("pop") ||
                        item.name.contains("rock") ||
                        item.name.contains("classic") ||
                        item.name.contains("adult contemporary") ||
                        item.name.contains("country") ||
                        item.name.contains("dance") ||
                        item.name.contains("electro") ||
                        item.name.contains("jazz") ||
                        item.name.contains("oldies") ||
                        item.name.contains("alternative") ||
                        item.name.contains("house") ||
                        item.name.contains("ambient") ||
                        item.name.contains("folk") ||
                        item.name.contains("blues") ||





                        item.name.contains("regional") ||
                        item.name.contains("entretenimiento") ||
                        item.name.contains("entertainment") ||
                        item.name.contains("easy listening") ||
                        item.name.contains("news") ||
                        item.name.contains("talk") ||
                        item.name.contains("speech") ||
                        item.name.contains("comedy") ||
                        item.name.contains("storytelling") ||
                        item.name.contains("podcast") ||
                        item.name.contains("culture") ||
                        item.name.contains("science") ||
                        item.name.contains("film") ||
                        item.name.contains("soundtrack") ||
                        item.name.contains("information") ||
                        item.name.contains("sport") ||
                        item.name.contains("tv") ||
                        item.name.contains("noticias") ||
                        item.name.contains("politics") ||
                        item.name.contains("education") ||
                        item.name.contains("moi merino") ||
                        item.name.contains("commercial") ||
                        item.name.contains("banda") ||
                        item.name.contains("balada") ||
                        item.name.contains("bible") ||
                        item.name.contains("christian") ||
                        item.name.contains("catholic") ||
                        item.name.contains("gospel") ||

                        item.name.contains("charts") ||
                        item.name.contains("chill") ||
                        item.name.contains("disco") ||
                        item.name.contains("eclectic") ||
                        item.name.contains("edm") ||
                        item.name.contains("funk") ||
                        item.name.contains("grupera") ||
                        item.name.contains("heavy metal") ||
                        item.name.contains("hip-hop") ||
                        item.name.contains("indie") ||
                        item.name.contains("instrumental") ||
                        item.name.contains("international") ||
                        item.name.contains("juvenil") ||
                        item.name.contains("kids") ||
                        item.name.contains("latin") ||
                        item.name.contains("spanish") ||
                        item.name.contains("bollywood") ||
                        item.name.contains("lounge") ||
                        item.name.contains("mainstream") ||
                        item.name.contains("metal") ||
                        item.name.contains("punk") ||
                        item.name.contains("rap") ||
                        item.name.contains("reggae") ||
                        item.name.contains("relax") ||
                        item.name.contains("religion") ||
                        item.name.contains("religious") ||
                        item.name.contains("retro") ||
                        item.name.contains("romantic") ||
                        item.name.contains("rnb") ||
                        item.name.contains("schlager") ||
                        item.name.contains("soul") ||
                        item.name.contains("techno") ||
                        item.name.contains("traffic") ||
                        item.name.contains("trance") ||
                        item.name.contains("urban") ||
                        item.name.contains("tropical") ||
                        item.name.contains("traditional") ||
                        item.name.contains("smooth") ||
                        item.name.contains("salsa") ||
                        item.name.contains("romántica") ||
                        item.name.contains("anime") ||
                        item.name.contains("children") ||
                        item.name.contains("christmas") ||
                        item.name.contains("club") ||
                        item.name.contains("contemporary") ||
                        item.name.contains("decades") ||
                        item.name.contains("downtempo") ||
                        item.name.contains("drum and bass") ||
                        item.name.contains("economics") ||
                        item.name.contains("experimental") ||
                        item.name.contains("freeform") ||
                        item.name.contains("gothic") ||
                        item.name.contains("grupero") ||
                        item.name.contains("hardcore") ||
                        item.name.contains("hiphop") ||
                        item.name.contains("industrial") ||
                        item.name.contains("lifestyle") ||
                        item.name.contains("literature") ||
                        item.name.contains("live") ||
                        item.name.contains("local") ||
                        item.name.contains("love") ||
                        item.name.contains("new wave") ||
                        item.name.contains("opera") ||
                        item.name.contains("party") ||
                        item.name.contains("r&b") ||
                        item.name.contains("salsa") ||
                        item.name.contains("independent") ||
                        item.name.contains("workout") ||
                        item.name.contains("vocal") ||
                        item.name.contains("underground") ||
                        item.name.contains("swing") ||
                        item.name.contains("meditation") ||
                        item.name.contains("nature") ||
                        item.name.contains("spiritual") ||
                        item.name.contains("slow") ||
                        item.name.contains("sleep") ||
                        item.name.contains("romance") ||
                        item.name.contains("remixes") ||
                        item.name.contains("progressive") ||
                        item.name.contains("nostalgia") ||
                        item.name.contains("new age") ||
                        item.name.contains("jungle") ||
                        item.name.contains("guitar") ||
                        item.name.contains("groove") ||
                        item.name.contains("garage") ||
                        item.name.contains("ethnic") ||
                        item.name.contains("dubstep") ||
                        item.name.contains("dj") ||
                        item.name.contains("avant-garde") ||
                        item.name.contains("acoustic") ||
                        item.name.contains("orchestral") ||
                        item.name.contains("piano") ||
                        item.name.contains("medieval") ||




                        item.name.contains("50s") ||
                        item.name.contains("60s") ||
                        item.name.contains("70s") ||
                        item.name.contains("80s") ||
                        item.name.contains("90s") ||
                        item.name.contains("00s") ||



                        item.name.contains("méxico") ||
                        item.name.contains("mexico") ||
                        item.name.contains("español") ||
                        item.name.contains("greek") ||
                        item.name.contains("norteamérica") ||
                        item.name.contains("latinoamérica")




                    ) {}

                  else {withoutObvious.add(item)}

            }

            Log.d("CHECKTAGS", "minus minors : ${withoutObvious.size}")


            withoutObvious.forEach {

                excluded += it.stationcount

                Log.d("CHECKTAGS", "name : ${it.name}, num : ${it.stationcount})")


            }


            Log.d("CHECKTAGS", "included stations : $included, excluded : $excluded")

        }


    }


}

