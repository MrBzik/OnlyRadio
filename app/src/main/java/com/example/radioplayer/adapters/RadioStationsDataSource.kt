package com.example.radioplayer.adapters

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.ui.viewmodels.MainViewModel

class RadioStationsDataSource (
    private val viewModel : MainViewModel,
    private val tag : String,
    private val name : String,
    private val country : String,
    )
            : PagingSource<Int, RadioStation>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RadioStation> {
        return try {
            val position = params.key ?: 0
            viewModel.searchWithNewParams(tag, country, name, position)

            LoadResult.Page(data = viewModel.mediaItems.value?.data!!, prevKey =
            if (position == 0) null
            else position - 10,
                nextKey = position+10)

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RadioStation>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(10)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(10)
        }
    }
}