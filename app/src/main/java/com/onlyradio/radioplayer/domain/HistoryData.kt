package com.onlyradio.radioplayer.domain

import androidx.paging.PagingData
import com.onlyradio.radioplayer.adapters.models.StationWithDateModel
import com.onlyradio.radioplayer.adapters.models.TitleWithDateModel
import com.onlyradio.radioplayer.data.local.entities.BookmarkedTitle

sealed class HistoryData {

    class StationsFlow(val data : PagingData<StationWithDateModel>) : HistoryData()

    class TitlesFlow(val data : PagingData<TitleWithDateModel>) : HistoryData()

    class Bookmarks(val list : List<BookmarkedTitle>) : HistoryData()


}
