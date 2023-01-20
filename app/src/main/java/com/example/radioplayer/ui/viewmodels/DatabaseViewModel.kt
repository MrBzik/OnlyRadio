package com.example.radioplayer.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.repositories.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatabaseViewModel @Inject constructor(
        private val repository: DatabaseRepository
) : ViewModel() {

    private val radioStations = repository.getAllStations()

    fun deleteStation (station: RadioStation) = viewModelScope.launch {
        repository.deleteRadioStation(station)
    }

    fun insertRadioStation (station: RadioStation) = viewModelScope.launch {
        repository.insertRadioStation(station)
    }


}