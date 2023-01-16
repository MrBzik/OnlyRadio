package com.example.radioplayer.ui.viewmodels

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.exoPlayer.RadioServiceConnection
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.exoPlayer.isPrepared
import com.example.radioplayer.utils.Constants.MEDIA_ROOT_ID
import com.example.radioplayer.utils.Constants.NEW_SEARCH
import com.example.radioplayer.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
       private val radioServiceConnection: RadioServiceConnection
) : ViewModel() {

       private val _mediaItems = MutableLiveData<Resource<List<RadioStation>>>()
       val mediaItems : LiveData<Resource<List<RadioStation>>> = _mediaItems

       val isConnected = radioServiceConnection.isConnected
       val currentRadioStation = radioServiceConnection.currentRadioStation
       val networkError = radioServiceConnection.networkError
       val playbackState = radioServiceConnection.playbackState


        fun searchWithNewParams(tag : String = "", country : String = "", name : String = "") {

            val bundle = Bundle().apply {

                if(tag == "none"){
                    putString("TAG", "")
                } else {
                    putString("TAG", tag)
                }

                if(country == "none"){
                    putString("COUNTRY", "")
                } else {
                    putString("COUNTRY", country)
                }

                if(name == "none"){
                    putString("NAME", "")
                } else {
                    putString("NAME", name)
                }

            }
            radioServiceConnection.sendCommand(NEW_SEARCH, bundle)

        }



       init {

           _mediaItems.postValue(Resource.loading(null))

           radioServiceConnection.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){

                  override fun onChildrenLoaded(
                         parentId: String,
                         children: MutableList<MediaBrowserCompat.MediaItem>
                  ) {
                         super.onChildrenLoaded(parentId, children)

                    val items =  children.map{

                            RadioStation(
                                   it.description.iconUri.toString(),
                                   it.description.title.toString(),
                                   it.mediaId,
                                   it.description.subtitle.toString(),
                                   it.description.mediaUri.toString()
                            )
                      }

                         _mediaItems.postValue(Resource.success(items))
                  }
            })
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