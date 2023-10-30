package com.onlyradio.radioplayer.repositories

import com.onlyradio.radioplayer.data.local.RadioDAO
import javax.inject.Inject

class LazyRepo @Inject constructor (private val radioDAO: RadioDAO) {

    suspend fun getStationsForDurationCheck() = radioDAO.getStationsForDurationCheck()

    suspend fun updateStationPlayDuration(newDuration : Long, stationId : String) =
        radioDAO.updateStationPlayDuration(newDuration, stationId)

    suspend fun updateRadioStationLastClicked (stationId : String) = radioDAO.updateStationLastClicked(
        System.currentTimeMillis(), stationId
    )

    suspend fun updateRadioStationPlayedDuration (stationId : String, duration : Long) =
        radioDAO.updateRadioStationPlayedDuration(stationId, duration)

    suspend fun setRadioStationPlayedDuration(stationID: String, duration: Long) =
        radioDAO.setRadioStationPlayedDuration(stationID, duration)


}