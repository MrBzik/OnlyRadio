package com.onlyradio.radioplayer.exoPlayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onlyradio.radioplayer.data.models.OnSnackRestore
import com.onlyradio.radioplayer.utils.Constants.NETWORK_ERROR
import com.onlyradio.radioplayer.utils.Event
import com.onlyradio.radioplayer.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class RadioServiceConnection (
    context : Context
        ) {


//    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
//    val isConnected : LiveData<Event<Resource<Boolean>>> = _isConnected

    companion object{
        var isConnected = false
    }


    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError : LiveData<Event<Resource<Boolean>>> = _networkError

    var isPlaybackStatePrepared = false

    private val _playState = MutableStateFlow(false)
    val isPlaying = _playState.asStateFlow()

    private val _currentRadioStation = MutableStateFlow<MediaMetadataCompat?>(null)
    val currentRadioStation = _currentRadioStation.asStateFlow()

    lateinit var mediaController : MediaControllerCompat

    private var mediaControllerCallback = MediaControllerCallback()

    lateinit var playerPosition : (() -> Int)


    val onSwipeHandled = Channel<OnSnackRestore>()


    fun setterForPlayerPos(pos : () -> Int){
        playerPosition = pos
    }
    fun getPlayerCurrentIndex() : Int {
        return playerPosition()
    }


    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    fun subscribe (parentId : String, callback : MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe (parentId : String) {
        mediaBrowser.unsubscribe(parentId)
    }

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, RadioService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }

    fun disconnectBrowser(){
        if(isConnected){
            mediaController.unregisterCallback(mediaControllerCallback)
            mediaBrowser.disconnect()
        }
    }

    fun connectBrowser(){
      try {
          mediaBrowser.connect()
      } catch (e : Exception){
          /*SILLY EXCEPTION*/
      }

    }

   private inner class MediaBrowserConnectionCallback (
       private val context: Context
           ) : MediaBrowserCompat.ConnectionCallback() {

       override fun onConnected() {
//           Log.d("CHECKTAGS", "on connected")
           mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
               registerCallback(mediaControllerCallback)
           }

           isConnected = true

//           _isConnected.postValue(
//               Event(Resource.success(true))
//           )
       }

       override fun onConnectionSuspended() {

           isConnected = false
//
//           _isConnected.postValue(Event(Resource.error(
//               "Connection was suspended", false
//           )))
       }

       override fun onConnectionFailed() {

           isConnected = false

//           _isConnected.postValue(Event(Resource.error(
//               "Connection failed", false
//           )))
       }



   }

   private inner class MediaControllerCallback : MediaControllerCompat.Callback(){


       override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
           state?.let {

               isPlaybackStatePrepared = state.isPrepared

               if(state.isPlaying)
                   _playState.value = true
               else if(state.isPlayEnabled)
                   _playState.value = false
           }

//
//           _playbackState.postValue(state)
       }

       override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
           _currentRadioStation.value = metadata
       }

       override fun onSessionEvent(event: String?, extras: Bundle?) {
           super.onSessionEvent(event, extras)
           when(event){
               NETWORK_ERROR -> _networkError.postValue(
                   Event(
                       Resource.error(
                           "Network error. Check your internet connection",
                           null
                       )
                   )
               )
           }
       }

       override fun onSessionDestroyed() {
           mediaBrowserConnectionCallback.onConnectionSuspended()
           mediaBrowser.disconnect()
       }

   }


    fun sendCommand(command: String, parameters: Bundle?) =
        sendCommand(command, parameters) { _, _ -> }

    private fun sendCommand(
        command: String,
        parameters: Bundle?,
        resultCallback: ((Int, Bundle?) -> Unit)
    ) = if (mediaBrowser.isConnected) {
        mediaController.sendCommand(command, parameters, object : ResultReceiver(Handler(Looper.getMainLooper())) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                resultCallback(resultCode, resultData)
            }

        })
        true
    } else {
        false
    }


}