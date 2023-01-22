package com.example.radioplayer.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.radioplayer.data.local.entities.RadioStation

@Dao
interface  RadioDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(station : RadioStation)

    @Delete
    suspend fun delete(station : RadioStation)

    @Query("SELECT * FROM radio_stations")
    fun  getAllStations() : LiveData<List<RadioStation>>

    @Query("SELECT * FROM radio_stations WHERE stationuuid ==:id")
    suspend fun checkIfExists (id : String) : RadioStation?


}