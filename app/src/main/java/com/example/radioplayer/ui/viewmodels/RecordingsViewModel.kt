package com.example.radioplayer.ui.viewmodels

import android.app.Application
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.RadioServiceConnection
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.exoPlayer.isPrepared
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.utils.Commands.COMMAND_REMOVE_RECORDING_MEDIA_ITEM
import com.example.radioplayer.utils.Commands.COMMAND_RESTORE_RECORDING_MEDIA_ITEM
import com.example.radioplayer.utils.Commands.COMMAND_START_RECORDING
import com.example.radioplayer.utils.Commands.COMMAND_STOP_RECORDING
import com.example.radioplayer.utils.Commands.COMMAND_UPDATE_REC_PLAYBACK_SPEED
import com.example.radioplayer.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingsViewModel @Inject constructor(
    private val app : Application,
    private val repository: DatabaseRepository,
    private val radioSource: RadioSource,
    private val radioServiceConnection: RadioServiceConnection

) : AndroidViewModel(app) {

    val playbackState = radioServiceConnection.playbackState

    // Recordings

//    fun insertNewRecording (
//        recordID : String,
//        iconURI : String,
//        name : String,
//        duration : String
//        ) = viewModelScope.launch {
//            repository.insertRecording(
//                Recording(
//                    id = recordID,
//                    iconUri = iconURI,
//                    name = name,
//                    timeStamp = System.currentTimeMillis(),
//                    duration = duration)
//            )
//    }


    var isRecordingsCheckNeeded = true

    val durationWithPosition = RadioService.recordingDuration
        .asFlow()
        .combine(RadioService.recordingPlaybackPosition.asFlow()){ dur, pos ->
            dur - pos
        }



    fun checkRecordingsForCleanUp(recList : List<Recording>){

        if(isRecordingsCheckNeeded){

            viewModelScope.launch(Dispatchers.IO){

                val fileList = app.fileList().filter {
                    it.endsWith(".ogg")
                }

                if(fileList.size > recList.size){

                    fileList.forEach { fileName ->

                        val rec = recList.find { rec ->
                            rec.id == fileName
                        }

                        if(rec == null){
                            app.deleteFile(fileName)
                        }
                    }
                }

                isRecordingsCheckNeeded = false
            }
        }
    }


    fun insertNewRecording(rec : Recording) =
        viewModelScope.launch {
            repository.insertRecording(rec)
        }

    fun deleteRecording(recId : String) = viewModelScope.launch {
        repository.deleteRecording(recId)
    }


    val allRecordingsLiveData = radioSource.allRecordingsLiveData



    fun removeRecordingFile(recordingID : String) = viewModelScope.launch(Dispatchers.IO) {
        app.deleteFile(recordingID)
    }


    fun renameRecording(id : String, newName: String) = viewModelScope.launch {
        repository.renameRecording(id, newName)
    }



    fun playOrToggleRecording(
        rec : Recording,
        playWhenReady : Boolean = true,
        itemIndex : Int? = -1
    ): Boolean {

        val isToChangeMediaItems = RadioService.currentMediaItems != Constants.SEARCH_FROM_RECORDINGS

        val isPrepared = playbackState.value?.isPrepared ?: false

        val id = rec.id

        if(isPrepared && id == RadioService.currentPlayingRecording.value?.id
            && RadioService.currentMediaItems == Constants.SEARCH_FROM_RECORDINGS
        ) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> {

                        radioServiceConnection.transportControls.pause()
                        return false
                    }

                    playbackState.isPlayEnabled -> {

                        radioServiceConnection.transportControls.play()
                        return true
                    }
                    else -> false
                }
            }

        } else {

            RadioService.currentMediaItems = Constants.SEARCH_FROM_RECORDINGS
            RadioService.recordingPlaybackPosition.postValue(0)

            radioServiceConnection.transportControls
                .playFromMediaId(id, bundleOf(
                    Pair(Constants.SEARCH_FLAG, Constants.SEARCH_FROM_RECORDINGS),
                    Pair(Constants.PLAY_WHEN_READY, playWhenReady),
                    Pair(Constants.ITEM_INDEX, itemIndex),
                    Pair(Constants.IS_CHANGE_MEDIA_ITEMS, isToChangeMediaItems)
                )
                )
        }

        return false
    }


    // ExoRecord

    val currentPlayerPosition = RadioService.recordingPlaybackPosition
    val currentRecordingDuration = RadioService.recordingDuration

    fun startRecording() {
        radioServiceConnection.sendCommand(COMMAND_START_RECORDING, null)
    }

    fun stopRecording(){
        radioServiceConnection.sendCommand(COMMAND_STOP_RECORDING, null)
    }
    val exoRecordFinishConverting = radioSource.exoRecordFinishConverting
    val exoRecordState = radioSource.exoRecordState
    val exoRecordTimer = radioSource.exoRecordTimer

//        val isRecordingUpdated = radioSource.isRecordingUpdated

    fun updateRecPlaybackSpeed(){
        radioServiceConnection.sendCommand(COMMAND_UPDATE_REC_PLAYBACK_SPEED, null)
    }


    fun seekTo(position : Long){
        radioServiceConnection.transportControls.seekTo(position)

    }


    fun removeRecordingMediaItem(index : Int){

        radioServiceConnection.sendCommand(
            COMMAND_REMOVE_RECORDING_MEDIA_ITEM,
            bundleOf(Pair(Constants.ITEM_INDEX, index)))
    }

    fun restoreRecordingMediaItem(index : Int){
        radioServiceConnection.sendCommand(
            COMMAND_RESTORE_RECORDING_MEDIA_ITEM,
            bundleOf(Pair(Constants.ITEM_INDEX, index)))
    }



}