package com.example.radioplayer.adapters.datasources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.radioplayer.data.local.entities.RadioStation


typealias StationsPageLoader = suspend (pageIndex : Int, pageSize : Int) -> List<RadioStation>


class RadioStationsDataSource (
   private val loader: StationsPageLoader,
   private val pageSize: Int,

    )
            : PagingSource<Int, RadioStation>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RadioStation> {

        val pageIndex = params.key ?: 0

        return try {

            val stations = loader(pageIndex, params.loadSize)

            val sortedStations = stations.distinctBy {
                it.name
            }

            LoadResult.Page(
                data = sortedStations,
                prevKey = if (pageIndex == 0) null else pageIndex - 1,
                nextKey = if (stations.size == params.loadSize) pageIndex + (params.loadSize / pageSize) else null
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RadioStation>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}