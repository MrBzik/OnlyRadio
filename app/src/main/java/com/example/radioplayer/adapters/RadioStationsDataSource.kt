package com.example.radioplayer.adapters

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.radioplayer.data.local.entities.RadioStation

typealias StationsPageLoader = suspend (pageIndex : Int, pageSize : Int) -> List<RadioStation>


class RadioStationsDataSource (
   private val loader: StationsPageLoader,
   private val pageSize: Int
    )
            : PagingSource<Int, RadioStation>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RadioStation> {

        val pageIndex = params.key ?: 0

        return try {

            val stations = loader(pageIndex, params.loadSize)

            LoadResult.Page(
                data = stations,
                prevKey = if (pageIndex == 0) null else pageIndex - 1,
                nextKey = pageIndex +1
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RadioStation>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

}