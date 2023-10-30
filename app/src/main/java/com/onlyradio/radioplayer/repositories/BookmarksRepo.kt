package com.onlyradio.radioplayer.repositories

import com.onlyradio.radioplayer.data.local.RadioDAO
import com.onlyradio.radioplayer.data.local.entities.BookmarkedTitle
import javax.inject.Inject

class BookmarksRepo @Inject constructor (private val radioDAO: RadioDAO) {

    fun bookmarkedTitlesLiveData() = radioDAO.bookmarkedTitlesLiveData()

    suspend fun deleteBookmarkTitle(title: BookmarkedTitle) = radioDAO.deleteBookmarkTitle(title)

    suspend fun deleteBookmarksByTitle(title : String) = radioDAO.deleteBookmarksByTitle(title)

    suspend fun insertNewBookmarkedTitle(title : BookmarkedTitle) = radioDAO.insertNewBookmarkedTitle(title)

    suspend fun countBookmarkedTitles() = radioDAO.countBookmarkedTitles()

    suspend fun getLastValidBookmarkedTitle(offset : Int) = radioDAO.getLastValidBookmarkedTitle(offset)

    suspend fun cleanBookmarkedTitles(timeStamp : Long) = radioDAO.cleanBookmarkedTitles(timeStamp)


}