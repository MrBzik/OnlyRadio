package com.example.radioplayer.repositories

import androidx.lifecycle.LiveData
import com.example.radioplayer.data.local.RadioDAO
import com.example.radioplayer.data.local.entities.RadioStation
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val radioDAO: RadioDAO
        ) {


    suspend fun insertRadioStation (station : RadioStation) = radioDAO.upsert(station)

    suspend fun deleteRadioStation (station: RadioStation) = radioDAO.delete(station)

    fun getAllStations () = radioDAO.getAllStations()


}