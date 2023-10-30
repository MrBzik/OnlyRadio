package com.onlyradio.radioplayer.repositories

import com.onlyradio.radioplayer.data.local.RadioDAO
import com.onlyradio.radioplayer.data.local.entities.Title
import javax.inject.Inject

class TitlesRepo @Inject constructor(private val radioDAO: RadioDAO) {

    suspend fun deleteTitlesWithDate(time : Long) = radioDAO.deleteTitlesWithDate(time)
    suspend fun insertNewTitle(title : Title) = radioDAO.insertNewTitle(title)
    suspend fun checkTitleTimestamp(title : String, date : Long) = radioDAO.checkTitleTimestamp(title, date)
    suspend fun deleteTitle(title : Title) = radioDAO.deleteTitle(title)

}