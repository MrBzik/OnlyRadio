package com.example.radioplayer.adapters.datasources

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.radioplayer.adapters.models.TitleWithDateModel



typealias TitlesPageLoader = suspend (pageIndex : Int, pageSize : Int) -> List<TitleWithDateModel>


class TitlesDataSource (
   private val loader: TitlesPageLoader,
   private val pageSize: Int,

    )
            : PagingSource<Int, TitleWithDateModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TitleWithDateModel> {

        val pageIndex = params.key ?: 0

        return try {

            val titles = loader(pageIndex, pageSize)

            LoadResult.Page(
                data = titles,
                prevKey = if (pageIndex == 0) null else pageIndex - 1,
                nextKey = if (titles.size < pageSize) null else pageIndex + 1
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TitleWithDateModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}