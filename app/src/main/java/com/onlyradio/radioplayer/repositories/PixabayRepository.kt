package com.onlyradio.radioplayer.repositories

import com.onlyradio.radioplayer.data.remote.PixabayApi
import com.onlyradio.radioplayer.data.remote.pixabay.PixabayResponse
import retrofit2.Response
import javax.inject.Inject

class PixabayRepository @Inject constructor(
        private val pixabayApi: PixabayApi
) {

    suspend fun searchForImages (pageIndex : Int, query : String) : Response<PixabayResponse>
            = pixabayApi.makeRequest(searchQuery = query, pageIndex = pageIndex)




}