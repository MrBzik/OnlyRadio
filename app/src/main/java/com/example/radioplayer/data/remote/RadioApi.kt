package com.example.radioplayer.data.remote

import com.example.radioplayer.data.remote.entities.RadioStations
import com.example.radioplayer.data.remote.entities.RadioTags
import com.example.radioplayer.utils.Constants.PAGE_SIZE
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface RadioApi {

        @POST("/json/stations/search")
        suspend fun searchRadio(

            @Query("countrycode")
            country : String,

            @Query("tag")
            tag : String,

            @Query("name")
            name : String = "",

            @Query("limit")
            limit : Int = PAGE_SIZE,

            @Query("offset")
            offset : Int,

            @Query("hidebroken")
            hidebroken : Boolean = true,

            @Query("order")
            sortBy : String = "votes"

        ) : Response<RadioStations>

    @POST("/json/stations/search")
    suspend fun searchRadioWithoutCountry(

        @Query("tag")
        tag : String,

        @Query("name")
        name : String = "",

        @Query("limit")
        limit : Int = PAGE_SIZE,

        @Query("offset")
        offset : Int,

        @Query("hidebroken")
        hidebroken : Boolean = true,

        @Query("order")
        sortBy : String = "votes"

    ) : Response<RadioStations>




        @POST("/json/stations/topvote")
        suspend fun getTopVotedStations(
            @Query("limit")
            limit : Int = PAGE_SIZE,

            @Query("offset")
            offset : Int = 0,

            @Query("hidebroken")
            hidebroken : Boolean = true
        ) : Response<RadioStations>

        @POST("/json/tags")
        suspend fun getAllTags() : Response<RadioTags>

}