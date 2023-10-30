package com.onlyradio.radioplayer.repositories

import com.onlyradio.radioplayer.data.local.RadioDAO
import com.onlyradio.radioplayer.data.local.entities.Recording
import javax.inject.Inject

class RecRepo @Inject constructor (private val radioDAO: RadioDAO) {

    suspend fun renameRecording(id : String, newName: String) = radioDAO.renameRecording(id, newName)

    suspend fun insertRecording(recording : Recording) = radioDAO.insertRecording(recording)

    suspend fun deleteRecording(recId : String) = radioDAO.deleteRecording(recId)

}