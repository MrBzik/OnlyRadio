package com.onlyradio.radioplayer.data.local.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.onlyradio.radioplayer.data.local.entities.HistoryDate
import com.onlyradio.radioplayer.data.local.entities.RadioStation

data class DateWithStations (
    @Embedded val date: HistoryDate,
    @Relation(
        parentColumn = "date",
        entityColumn = "stationuuid",
        associateBy = Junction(StationDateCrossRef::class)
    )
        val radioStations : List<RadioStation>

        )