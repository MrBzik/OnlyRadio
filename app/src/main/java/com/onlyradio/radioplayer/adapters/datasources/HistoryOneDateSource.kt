package com.onlyradio.radioplayer.adapters.datasources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.onlyradio.radioplayer.adapters.models.StationWithDateModel



typealias HistoryOneDateLoader = suspend () -> List<StationWithDateModel>

class HistoryOneDateSource(
   private val loader: HistoryOneDateLoader
) : PagingSource<Int, StationWithDateModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StationWithDateModel> {


        return try {

            val stations = loader()

            LoadResult.Page(
                data = stations,
                prevKey = null,
                nextKey = null
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    }


    override fun getRefreshKey(state: PagingState<Int, StationWithDateModel>): Int? {
        return 0
    }
}