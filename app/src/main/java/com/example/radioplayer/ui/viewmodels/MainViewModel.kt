package com.example.radioplayer.ui.viewmodels

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.example.radioplayer.adapters.RadioStationsDataSource
import com.example.radioplayer.adapters.StationsPageLoader
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.exoPlayer.*
import com.example.radioplayer.utils.Constants.MEDIA_ROOT_ID
import com.example.radioplayer.utils.Constants.NEW_SEARCH
import com.example.radioplayer.utils.Constants.PAGE_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
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
       private val list = mutableListOf<RadioStation>()


       private suspend fun searchWithNewParams(
            limit : Int, offset : Int, bundle: Bundle
        ) : List<RadioStation> {

           withContext(Dispatchers.IO) {

               val calcOffset = limit * offset

               bundle.apply {

                   val tag = this.getString("TAG") ?: ""
                   val name = this.getString("NAME") ?: ""
                   val country = getString("COUNTRY") ?: ""
                   val isTopSearch = this.getBoolean("SEARCH_TOP")

                   this.putInt("OFFSET", calcOffset)

                  val response = radioSource.getRadioStationsSource(
                       offset = calcOffset,
                       isTopSearch = isTopSearch,
                       country = country,
                       tag = tag,
                       name = name)


                   response?.forEach {
                       list.add(
                           RadioStation(
                           favicon = it.favicon,
                           name = it.name,
                           stationuuid = it.stationuuid,
                           country = it.country,
                           url = it.url_resolved)
                       )
                   }

               }

       }

           radioServiceConnection.sendCommand(NEW_SEARCH, bundle)

           return list

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


    val stationsFlow : Flow<PagingData<RadioStation>>

    private val searchBy = MutableLiveData(Bundle())

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

       init {

           radioServiceConnection.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){
               override fun onChildrenLoaded(
                   parentId: String,
                   children: MutableList<MediaBrowserCompat.MediaItem>
               ) {
                   super.onChildrenLoaded(parentId, children)
               }

           })

           stationsFlow = searchBy.asFlow()
               .flatMapLatest {
                   searchStationsPaging(it)
               }
               .cachedIn(viewModelScope)

       }


        fun playOrToggleStation(mediaItem : RadioStation, toggle : Boolean = false) {

            val isPrepared = playbackState.value?.isPrepared ?: false

            if(isPrepared && mediaItem.stationuuid
                    == currentRadioStation.value?.getString(METADATA_KEY_MEDIA_ID)){
                playbackState.value?.let { playbackState ->
                    when {
                        playbackState.isPlaying -> if(toggle) radioServiceConnection.transportControls.pause()
                        playbackState.isPlayEnabled -> radioServiceConnection.transportControls.play()
                        else -> Unit
                    }
                }
            } else{
                radioServiceConnection.transportControls.playFromMediaId(mediaItem.stationuuid, null)
            }
        }


       override fun onCleared() {
              super.onCleared()

              radioServiceConnection.unsubscribe(MEDIA_ROOT_ID)
       }

}