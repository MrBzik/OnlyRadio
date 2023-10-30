package com.onlyradio.radioplayer.repositories

import com.onlyradio.radioplayer.data.local.RadioDAO
import com.onlyradio.radioplayer.data.local.entities.HistoryDate
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.data.local.relations.StationDateCrossRef
import javax.inject.Inject

class DatesRepo @Inject constructor(private val radioDAO: RadioDAO) {


    suspend fun insertNewDate(date : HistoryDate) = radioDAO.insertNewDate(date)

    suspend fun insertStationDateCrossRef(stationDateCrossRef: StationDateCrossRef)
            = radioDAO.insertStationDateCrossRef(stationDateCrossRef)

    suspend fun getLastDate() = radioDAO.getLastDate()



    // Cleaning unused stations

    suspend fun gatherStationsForCleaning() : List<RadioStation>
            = radioDAO.gatherStationsForCleaning()

    suspend fun checkIfRadioStationInHistory(stationID : String) : Boolean
            = radioDAO.checkIfRadioStationInHistory(stationID)

    suspend fun deleteRadioStation (station : RadioStation) = radioDAO.deleteRadioStation(station)


    // Cleaning dates

    suspend fun getNumberOfDates() : Int = radioDAO.getNumberOfDates()

    suspend fun getDatesToDelete(limit: Int) : List<HistoryDate> = radioDAO.getDatesToDelete(limit)

    suspend fun deleteAllCrossRefWithDate(date : String) = radioDAO.deleteAllCrossRefWithDate(date)

    suspend fun deleteDate(date : HistoryDate) = radioDAO.deleteDate(date)


}