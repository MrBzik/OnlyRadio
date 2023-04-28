package com.example.radioplayer.exoPlayer

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
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.radioplayer.utils.Constants.NETWORK_ERROR
import com.example.radioplayer.utils.Event
import com.example.radioplayer.utils.Resource
import dagger.hilt.android.AndroidEntryPoint


class RadioServiceConnection (
    context : Context
        ) {


    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected : LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError : LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState : LiveData<PlaybackStateCompat?> = _playbackState

    private val _currentRadioStation = MutableLiveData<MediaMetadataCompat?>()
    val currentRadioStation : LiveData<MediaMetadataCompat?> = _currentRadioStation

    lateinit var mediaController : MediaControllerCompat


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

    fun disconnect(){
        mediaBrowser.disconnect()
    }

   private inner class MediaBrowserConnectionCallback (
       private val context: Context
           ) : MediaBrowserCompat.ConnectionCallback() {

       override fun onConnected() {
           mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
               registerCallback(MediaControllerCallback())
           }

           _isConnected.postValue(
               Event(Resource.success(true))
           )
       }

       override fun onConnectionSuspended() {
           _isConnected.postValue(Event(Resource.error(
               "Connection was suspended", false
           )))
       }

       override fun onConnectionFailed() {
           _isConnected.postValue(Event(Resource.error(
               "Connection failed", false
           )))
       }

   }

   private inner class MediaControllerCallback : MediaControllerCompat.Callback(){



       override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
           _playbackState.postValue(state)
       }

       override fun onMetadataChanged(metadata: MediaMetadataCompat?) {

           _currentRadioStation.postValue(metadata)
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