package com.example.radioplayer.data.remote

import com.example.radioplayer.data.remote.entities.Countries
import com.example.radioplayer.data.remote.entities.Languages
import com.example.radioplayer.data.remote.entities.RadioStations
import com.example.radioplayer.data.remote.entities.RadioTags
import com.example.radioplayer.utils.Constants.PAGE_SIZE
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url


interface RadioApi {

        @POST()
        suspend fun searchRadio(

            @Url url : String,

            @Query("countrycode")
            country : String,

            @Query("language")
            language : String = "",

            @Query("tag")
            tag : String,

            @Query("tagExact")
            tagExact : Boolean,

            @Query("name")
            name : String = "",

            @Query("nameExact")
            nameExact : Boolean,

            @Query("limit")
            limit : Int = PAGE_SIZE,

            @Query("offset")
            offset : Int,

            @Query("hidebroken")
            hidebroken : Boolean = true,

            @Query("order")
            sortBy : String,

            @Query("reverse")
            isReversed : Boolean = true,

            @Query("bitrateMin")
            bitrateMin : Int,

            @Query("bitrateMax")
            bitrateMax : Int

        ) : Response<RadioStations>

    @POST()
    suspend fun searchRadioWithoutCountry(

        @Url url : String,

        @Query("tag")
        tag : String,

        @Query("tagExact")
        tagExact : Boolean,

        @Query("language")
        language : String = "",

        @Query("name")
        name : String = "",

        @Query("nameExact")
        nameExact : Boolean,

        @Query("limit")
        limit : Int = PAGE_SIZE,

        @Query("offset")
        offset : Int,

        @Query("hidebroken")
        hidebroken : Boolean = true,

        @Query("order")
        sortBy : String,

        @Query("reverse")
        isReversed : Boolean = true,

        @Query("bitrateMin")
        bitrateMin : Int,

        @Query("bitrateMax")
        bitrateMax : Int

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
        suspend fun getAllTags() : Response<RadioTags>?


        @POST()
        suspend fun getAllCountries(
            @Url url : String
        ) : Response<Countries>

        @POST
        suspend fun getLanguages(
            @Url url : String
        ) : Response<Languages>


}