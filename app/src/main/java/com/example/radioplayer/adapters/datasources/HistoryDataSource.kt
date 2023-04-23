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
        Log.d("CHECKTAGS", "history1")
        var pagesLoaded = 1

        return try {

            Log.d("CHECKTAGS", "history2")

            val stations = loader(dateIndex).toMutableList()

            if(HistoryFragment.isNewHistoryQuery){
                while(stations.size < 9 && HistoryFragment.numberOfDates > pagesLoaded){

                    Log.d("CHECKTAGS", "history3")

                    val moreStations = loader(dateIndex+pagesLoaded)

                    if(moreStations.isEmpty()) break

                    stations.addAll(moreStations)

                    pagesLoaded += 1



                 }


                HistoryFragment.isNewHistoryQuery = false
            }



            LoadResult.Page(
                data = stations,
                prevKey = if (dateIndex == 0) null else dateIndex - 1,
                nextKey = dateIndex +pagesLoaded
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