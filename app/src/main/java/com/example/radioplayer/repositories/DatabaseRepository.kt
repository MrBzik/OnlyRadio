package com.example.radioplayer.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.radioplayer.data.local.RadioDAO
import com.example.radioplayer.data.local.entities.RadioStation
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val radioDAO: RadioDAO
        ) {


    suspend fun insertRadioStation (station : RadioStation) = radioDAO.upsert(station)

    suspend fun deleteRadioStation (station: RadioStation) = radioDAO.delete(station)

    suspend fun getAllStations () = radioDAO.getAllStations()


    suspend fun checkIfExists (id : String) = radioDAO.checkIfExists(id)


}