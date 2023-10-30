package com.onlyradio.radioplayer.repositories

import com.onlyradio.radioplayer.data.local.RadioDAO
import javax.inject.Inject

class TitlesDatesRepo @Inject constructor(private val radioDAO: RadioDAO) {
    suspend fun getTitlesPage(offset: Int, limit: Int) = radioDAO.getTitlesPage(offset, limit)

    suspend fun getTitlesInOneDatePage(offset: Int, limit: Int, date: Long) =
        radioDAO.getTitlesInOneDatePage(offset, limit, date)

    val getListOfDates = radioDAO.getListOfDates()

}