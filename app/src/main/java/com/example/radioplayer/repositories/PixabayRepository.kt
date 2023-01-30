package com.example.radioplayer.repositories

import com.example.radioplayer.data.remote.PixabayApi
import com.example.radioplayer.data.remote.pixabay.PixabayResponse
import com.example.radioplayer.utils.Resource
import retrofit2.Response
import javax.inject.Inject

class PixabayRepository @Inject constructor(
        private val pixabayApi: PixabayApi
) {

    suspend fun searchForImages (pageIndex : Int, query : String) : Response<PixabayResponse>
            = pixabayApi.makeRequest(searchQuery = query, pageIndex = pageIndex)




}