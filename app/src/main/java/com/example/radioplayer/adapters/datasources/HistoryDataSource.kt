package com.example.radioplayer.adapters.datasources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.radioplayer.adapters.models.StationWithDateModel



typealias HistoryDateLoader = suspend (dateIndex : Int) -> List<StationWithDateModel>

class HistoryDataSource(
   private val loader: HistoryDateLoader
) : PagingSource<Int, StationWithDateModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StationWithDateModel> {

        val dateIndex = params.key ?: 0

        return try {

            val stations = loader(dateIndex)

            LoadResult.Page(
                data = stations,
                prevKey = if (dateIndex == 0) null else dateIndex - 1,
                nextKey = dateIndex + 1
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    }


    override fun getRefreshKey(state: PagingState<Int, StationWithDateModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}