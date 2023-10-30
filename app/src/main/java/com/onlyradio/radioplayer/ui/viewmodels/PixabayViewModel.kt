package com.onlyradio.radioplayer.ui.viewmodels


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.onlyradio.radioplayer.adapters.datasources.ImagesPageLoader
import com.onlyradio.radioplayer.adapters.datasources.PixabayDataSource
import com.onlyradio.radioplayer.data.remote.pixabay.Hit
import com.onlyradio.radioplayer.repositories.PixabayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject


@HiltViewModel
class PixabayViewModel @Inject constructor(
    private val repository: PixabayRepository
) : ViewModel() {


    val togglePlaylistsVisibility : MutableLiveData<Boolean> = MutableLiveData(false)

    private val imageSearchBy : MutableLiveData<String> = MutableLiveData("music")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val imagesFlow = imageSearchBy.asFlow()
        .debounce(800)
        .flatMapLatest {
            searchForImages(it)
        }
        .cachedIn(viewModelScope)

     fun setImageSearch(searchQuery: String){

        if(searchQuery == imageSearchBy.value){
            return
        } else {
            imageSearchBy.value = searchQuery
        }
    }


    private fun searchForImages(
        searchQuery : String
    ): Flow<PagingData<Hit>> {
        val loader : ImagesPageLoader = { pageIndex ->
            repository.searchForImages(pageIndex, searchQuery).body()?.hits ?: emptyList()
        }

        return Pager(
                config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                PixabayDataSource(loader)
            }
        ).flow
    }




}