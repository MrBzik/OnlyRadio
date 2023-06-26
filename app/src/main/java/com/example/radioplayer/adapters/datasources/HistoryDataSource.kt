package com.example.radioplayer.adapters.datasources

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.radioplayer.adapters.models.StationWithDateModel
import com.example.radioplayer.ui.fragments.HistoryFragment


typealias HistoryDateLoader = suspend (dateIndex : Int) -> List<StationWithDateModel>

class HistoryDataSource(
   private val loader: HistoryDateLoader
) : PagingSource<Int, StationWithDateModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StationWithDateModel> {

        val dateIndex = params.key ?: 0

//        var pagesLoaded = 1

        return try {

            val stations = loader(dateIndex).toMutableList()

//            if(HistoryFragment.isNewHistoryQuery){
//                while(stations.size < 9 && HistoryFragment.numberOfDates > pagesLoaded){
//
//                    val moreStations = loader(dateIndex+pagesLoaded)
//
//                    stations.addAll(moreStations)
//
//                    pagesLoaded += 1
//
//                 }
//
//
//                HistoryFragment.isNewHistoryQuery = false
//            }

            val nextKey = dateIndex + 1

            LoadResult.Page(
                data = stations,
                prevKey = if (dateIndex == 0) null else dateIndex - 1,
                nextKey = if(nextKey < HistoryFragment.numberOfDates) nextKey
                            else null
            )

        } catch (e: Exception) {
            Log.d("CHECKTAGS", e.stackTraceToString())
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