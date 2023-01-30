package com.example.radioplayer.data.remote

import com.example.radioplayer.BuildConfig
import com.example.radioplayer.data.remote.pixabay.PixabayResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayApi {


    @GET("/api/")
    suspend fun makeRequest(

        @Query("key")
        key : String = BuildConfig.API_KEY,

        @Query("q")
        searchQuery : String,

        @Query("category")
        category : String = "music",

        @Query("page")
        pageIndex : Int = 1

    ) : Response<PixabayResponse>


}