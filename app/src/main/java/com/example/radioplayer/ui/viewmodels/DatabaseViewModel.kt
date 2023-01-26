package com.example.radioplayer.ui.viewmodels

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.exoPlayer.RadioServiceConnection
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.COMMAND_LOAD_FROM_DB
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatabaseViewModel @Inject constructor(
        private val repository: DatabaseRepository,
        private val radioSource: RadioSource,
) : ViewModel() {

    val getAllStationsTEST = radioSource.getAllItemsTEST()


    val isExisting : MutableLiveData<Boolean> = MutableLiveData()


    fun ifAlreadyInDatabase(id : String) = viewModelScope.launch {
       val check = repository.checkIfExists(id)
        isExisting.postValue(check != null)
    }


    fun deleteStation (station: RadioStation) = viewModelScope.launch {
        repository.deleteRadioStation(station)
    }

    fun insertRadioStation (station: RadioStation) = viewModelScope.launch {
        repository.insertRadioStation(station)
    }


}