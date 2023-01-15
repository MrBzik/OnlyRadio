package com.example.radioplayer.data.remote

import com.example.radioplayer.data.remote.entities.RadioStations
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface RadioApi {

        @POST("/json/stations/search")
        suspend fun searchRadio(

            @Query("country")
            country : String,

            @Query("tag")
            tag : String,

            @Query("limit")
            limit : Int = 10,

            @Query("offset")
            offset : Int = 0

        ) : Response<RadioStations>


}