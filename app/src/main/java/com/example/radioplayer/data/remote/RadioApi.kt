package com.example.radioplayer.data.remote

import com.example.radioplayer.data.remote.entities.Countries
import com.example.radioplayer.data.remote.entities.RadioStations
import com.example.radioplayer.data.remote.entities.RadioTags
import com.example.radioplayer.utils.Constants.PAGE_SIZE
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url


interface RadioApi {

        @POST()
        suspend fun searchRadio(

            @Url url : String,

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
            sortBy : String = "votes",

            @Query("reverse")
            isReversed : Boolean = true

        ) : Response<RadioStations>

    @POST()
    suspend fun searchRadioWithoutCountry(

        @Url url : String,

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
        sortBy : String = "votes",

        @Query("reverse")
        isReversed : Boolean = true

    ) : Response<RadioStations>


        @POST()
        suspend fun getTopVotedStations(

            @Url url: String,

            @Query("limit")
            limit : Int = PAGE_SIZE,

            @Query("offset")
            offset : Int = 0,

            @Query("hidebroken")
            hidebroken : Boolean = true
        ) : Response<RadioStations>




        @POST("/json/tags")
        suspend fun getAllTags() : Response<RadioTags>

        @POST("/json/countries")
        suspend fun getAllCountries() : Response<Countries>

}