package com.example.radioplayer.adapters.datasources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.radioplayer.data.remote.pixabay.Hit

typealias ImagesPageLoader = suspend (pageIndex : Int) -> List<Hit>

class PixabayDataSource (
    private val imagesPageLoader: ImagesPageLoader
        ) : PagingSource<Int, Hit>() {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Hit> {

        val pageIndex = params.key ?: 1

        return try {

            val images = imagesPageLoader(pageIndex)

            LoadResult.Page(
                data = images,
                prevKey = if (pageIndex == 1) null else pageIndex - 1,
                nextKey = if (images.size == params.loadSize) pageIndex + 1 else null
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }


    }



    override fun getRefreshKey(state: PagingState<Int, Hit>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}