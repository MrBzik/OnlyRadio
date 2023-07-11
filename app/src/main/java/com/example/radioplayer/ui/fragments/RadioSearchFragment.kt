package com.example.radioplayer.ui.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PagingRadioAdapter
import com.example.radioplayer.adapters.models.CountryWithRegion
import com.example.radioplayer.adapters.models.TagWithGenre
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.FragmentRadioSearchBinding
import com.example.radioplayer.databinding.StubNoResultMessageBinding
import com.example.radioplayer.databinding.StubTvTitleBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.TextLoadAnim
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.dialogs.*
import com.example.radioplayer.ui.stubs.NoResultMessage
import com.example.radioplayer.utils.*
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class RadioSearchFragment : BaseFragment<FragmentRadioSearchBinding>(
    FragmentRadioSearchBinding::inflate
) {


    private var isNewSearchForAnimations = true

    private var isInitialLaunch = true

    @Inject
    lateinit var pagingRadioAdapter : PagingRadioAdapter

    private var textLoadAnim : TextLoadAnim? = null

    private var isToShowLoadingMessage = false

    private var isToHandleNewStationObserver = false

    private var isToInitiateNewSearch = false

    private var bindNoResultMessage : StubNoResultMessageBinding? = null

    private var isBindNoResultMessageInflated = false

    private var noResultMessage = NoResultMessage(
        postInitiateNewSearch = {isToInitiateNewSearch = true},
        initiateNewSearch = { initiateNewSearch() }
    )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.stubTvNoResultMessage.setOnInflateListener{ _, bindView ->
            bindNoResultMessage = StubNoResultMessageBinding.bind(bindView)
        }

        setSearchParamsObservers()

        setSearchToolbar()

        setRecycleView()

        subscribeToStationsFlow()

        observePlaybackState()

        observeNewStation()

        setRecyclerViewAttachChildrenListener()

        setAdapterOnClickListener()

        setOnRefreshSearch()

        setSearchButton()

        observeNoResultDetector()

        setToolbar()

        setSearchParamsFabClickListener()

        observeInternetConnection()

        observeSearchState()

        textLoadAnim = TextLoadAnim(requireContext(), bind.tvLoading)

    }


    private fun setSearchButton(){

        if(!mainViewModel.isFullAutoSearch){
//            setDragListenerForLayout()
//            setDragListenerForButton()
//            getFabSearchPositionIfNeeded()
            listenSearchButton()
        }
    }


    private fun setSearchParamsFabClickListener(){

        bind.fabSearchOrder.setOnClickListener {
            SearchParamsDialog(
                requireContext = requireContext(),
                mainViewModel = mainViewModel,
                searchDialogsViewModel = searchDialogsViewModels
            ) {
                initiateNewSearch()
            }.show()
        }
    }


    private fun initiateNewSearch(){
        if(mainViewModel.isFullAutoSearch){
            val check = mainViewModel.initiateNewSearch()
            clearAdapter(check)
        }
    }

    private fun setToolbar(){
        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            bind.viewToolbar.setBackgroundResource(R.drawable.toolbar_search_vector)
        }
        else {
            bind.viewToolbar.setBackgroundColor(Color.BLACK)
        }
    }


    private fun observeNoResultDetector(){
        mainViewModel.noResultDetection.observe(viewLifecycleOwner){ noResult ->
            if(noResult){
                if(!isBindNoResultMessageInflated){
                    isBindNoResultMessageInflated = true
                    bind.stubTvNoResultMessage.inflate()
                }

               bindNoResultMessage?.let {
                   noResultMessage.generateMessage(it, mainViewModel)
               }
            }
        }
    }


    private fun observeNewStation(){

        RadioService.currentPlayingStation.observe(viewLifecycleOwner){ station ->

        if(RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS){

            if(isToHandleNewStationObserver){

                val index = getCurrentItemPosition(station)

                if(index != -1){
                    handleNewRadioStation(index, station)
                } else {
                    pagingRadioAdapter.updateOnStationChange(station, null)
                }
            }

            else {
                isToHandleNewStationObserver = true
            }
        }
    }
}


    private fun getCurrentItemPosition(station : RadioStation?) : Int {

        if(RadioService.currentMediaItems == SEARCH_FROM_API ){

            return RadioService.currentPlayingItemPosition

        } else {

            val index = pagingRadioAdapter.snapshot().items
                .indexOfFirst {

                    it.stationuuid == (station?.stationuuid
                        ?: RadioService.currentPlayingStation.value?.stationuuid)
                }

            return index
        }

    }


    private fun handleNewRadioStation(position : Int, station : RadioStation){

        bind.rvSearchStations.apply {

            post {
                scrollToPosition(position)

                post {
                    val holder = findViewHolderForAdapterPosition(position)

                    holder?.let {
                        pagingRadioAdapter.updateOnStationChange(station, holder as PagingRadioAdapter.RadioItemHolder)
                    }
                }
            }
        }
    }


    private fun observePlaybackState(){
        mainViewModel.playbackState.observe(viewLifecycleOwner){
            if(!RadioService.isFromRecording){
                it?.let {

                    when{
                        it.isPlaying -> {
                            pagingRadioAdapter.utils.currentPlaybackState = true

                            pagingRadioAdapter.updateStationPlaybackState()

                        }
                        it.isPlayEnabled -> {
                            pagingRadioAdapter.utils.currentPlaybackState = false

                            pagingRadioAdapter.updateStationPlaybackState()
                        }
                    }
                }
            }
        }
    }



//    private fun getFabSearchPositionIfNeeded(){
//        bind.fabInitiateSearch.doOnLayout {
//            if (mainViewModel.isFabMoved || mainViewModel.isFabUpdated) {
//                bind.fabInitiateSearch.x = mainViewModel.fabX
//                bind.fabInitiateSearch.y = mainViewModel.fabY
//            }
//            bind.fabInitiateSearch.isVisible = true
//        }
//    }

//    private fun setDragListenerForLayout(){
//            var tempX = 0f
//            var tempY = 0f
//
//        bind.root.setOnDragListener { v, event ->
//            when(event.action){
//                DragEvent.ACTION_DRAG_LOCATION -> {
//                   tempX = event.x
//                   tempY = event.y
//                }
//                DragEvent.ACTION_DRAG_ENDED ->{
//
//                    bind.fabInitiateSearch.x = tempX - bind.fabInitiateSearch.width/2
//                    bind.fabInitiateSearch.y = tempY - bind.fabInitiateSearch.height/2
//
//                    mainViewModel.fabX = bind.fabInitiateSearch.x
//                    mainViewModel.fabY = bind.fabInitiateSearch.y
//                    mainViewModel.isFabUpdated = true
//                }
//            }
//            true
//        }
//    }

//    private fun setDragListenerForButton(){
//        bind.fabInitiateSearch.setOnLongClickListener { view ->
//            val shadow = View.DragShadowBuilder(bind.fabInitiateSearch)
//            view.startDragAndDrop(null, shadow, view, 0)
//            true
//        }
//    }


    private fun setAdapterOnClickListener(){

        pagingRadioAdapter.utils.setOnClickListener { station, index ->

           val isToChangeMediaItems = RadioService.currentMediaItems != SEARCH_FROM_API

            mainViewModel.playOrToggleStation(station, SEARCH_FROM_API,
                itemIndex = index, isToChangeMediaItems = isToChangeMediaItems)
        }
    }


    private fun setRecycleView(){

        bind.rvSearchStations.apply {

            adapter = pagingRadioAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)

            setAdapterValues(pagingRadioAdapter.utils)



//            itemAnimator = null

            layoutAnimation = (activity as MainActivity).layoutAnimationController

            if(RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS){
                RadioService.currentPlayingStation.value?.let {
                    val id =  it.stationuuid
                    pagingRadioAdapter.currentRadioStationId = id
                }
            } else {
                pagingRadioAdapter.currentRadioStationId = ""
            }
        }
    }

    private fun observeSearchState(){

        mainViewModel.searchLoadingState.observe(viewLifecycleOwner){


            if(it){

                if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
                    (activity as MainActivity).startSeparatorsLoadAnim()
                } else {

                    if(mainViewModel.isNewSearch)
                        (activity as MainActivity).bind.progressBarBottom?.hide()
                    else
                        (activity as MainActivity).bind.progressBarBottom?.show()
                }


            } else if(!it){
                if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
                    (activity as MainActivity).endSeparatorsLoadAnim()
                } else {
                    (activity as MainActivity).bind.progressBarBottom?.hide()
                }

                textLoadAnim?.endLoadingAnim()

            }
        }
    }



    private fun setRecyclerViewAttachChildrenListener(){
        bind.rvSearchStations.addOnChildAttachStateChangeListener(
            object : RecyclerView.OnChildAttachStateChangeListener{
                override fun onChildViewAttachedToWindow(view: View) {

                    if(isNewSearchForAnimations){

                        isNewSearchForAnimations = false
                        isToShowLoadingMessage = false

                        bind.rvSearchStations.apply {
                            if(isInitialLaunch){

                                if(RadioService.currentPlayingItemPosition == -1){
                                    startLayoutAnimation()
                                } else {

                                    post {
                                        val index = getCurrentItemPosition(null)

                                        if(index != -1 ) scrollToPosition(index)

                                        startLayoutAnimation()
                                    }

                                }

                                isInitialLaunch = false

                            } else {
                                scrollToPosition(0)
                                startLayoutAnimation()
                            }
                        }

                    }

                }

                override fun onChildViewDetachedFromWindow(view: View) {

                }
            }
        )

    }


    private fun subscribeToStationsFlow(){

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                mainViewModel.stationsFlow.collectLatest {


//                if(!isInitialLaunch){
//                    launchRecyclerOutAnim()
//                }

                    if(isToShowLoadingMessage || mainViewModel.isInitialLaunchOfTheApp){
                        mainViewModel.isInitialLaunchOfTheApp = false

                        textLoadAnim?.startLoadingAnim()

//                    if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES)
//                        showLoadingResultsMessage()
//                    else {
//                        bind.tvResultMessage.visibility = View.INVISIBLE
//                    }
                    }

                    pagingRadioAdapter.submitData(it)

                }
            }
        }
    }



//    private fun showLoadingResultsMessage(){
//        bind.tvResultMessage.apply {
//            visibility = View.VISIBLE
//            text = "Waiting for response from servers..."
//            slideAnim(100, 0, R.anim.fade_in_anim)
//        }
//    }


    private fun setSearchToolbar() {

        bind.tvTag.setOnClickListener {
           TagPickerDialog(requireContext(), mainViewModel, searchDialogsViewModels){
               if(mainViewModel.isFullAutoSearch)
                   isToInitiateNewSearch = true
           }.show()
        }



        bind.tvName.setOnClickListener {

            NameAutoDialog(requireContext(), mainViewModel){
                initiateNewSearch()
            }.show()



//            if(mainViewModel.isNameAutoSearch){
//                NameAutoDialog(requireContext(), mainViewModel){
//
//                    val check = mainViewModel.initiateNewSearch()
//                    clearAdapter(check)
//
//                }.show()
//            } else {
//                NameDialog(requireContext(), mainViewModel){
//                    if(mainViewModel.isFullAutoSearch)
//                        isToInitiateNewSearch = true
//                }.show()
//            }

        }

        bind.tvSelectedCountry.setOnClickListener {

            CountryPickerDialog(requireContext(), mainViewModel, searchDialogsViewModels){
                if(mainViewModel.isFullAutoSearch)
                    isToInitiateNewSearch = true
            }.show()

        }
    }


    private fun setOnRefreshSearch(){

        if(mainViewModel.isFullAutoSearch)
        bind.swipeRefresh.isEnabled = false
        else {
            bind.swipeRefresh.setOnRefreshListener {

                val check = mainViewModel.initiateNewSearch()

                bind.swipeRefresh.isRefreshing = false

                clearAdapter(check)
                if(!check){
                    Toast.makeText(requireContext(), "Same search query", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun listenSearchButton(){

        bind.fabInitiateSearch.visibility = View.VISIBLE

        bind.fabInitiateSearch.setOnClickListener {

          val check =  mainViewModel.initiateNewSearch()
            clearAdapter(check)
          if(!check){
              Toast.makeText(requireContext(), "Same search query", Toast.LENGTH_SHORT).show()
          }

        }
    }

    private fun clearAdapter(check : Boolean){
        if(check){

            bindNoResultMessage?.apply {
                if(llRootLayout.isVisible){
                    llRootLayout.visibility = View.GONE
                    llRootLayout.slideAnim(150, 0, R.anim.fade_out_anim)
                }
            }

            isNewSearchForAnimations = true
            isToShowLoadingMessage = true
            pagingRadioAdapter.previousItemHolder = null
//            pagingRadioAdapter.submitData(lifecycle, PagingData.empty())
        }
    }


    private fun setSearchParamsObservers(){

        mainViewModel.searchParamTag.observe(viewLifecycleOwner){

            handleNewParams()

            (bind.tvTag as TextView).text  = it.ifBlank { "Tag" }

        }

        mainViewModel.searchParamName.observe(viewLifecycleOwner){

            if(noResultMessage.isNoResultClick){
                noResultMessage.isNoResultClick = false
                handleNewParams()
            }

            (bind.tvName as TextView).text = it.ifBlank { "Name" }

        }

        mainViewModel.searchParamCountry.observe(viewLifecycleOwner){

            handleNewParams()

            (bind.tvSelectedCountry as TextView).text = it.ifBlank {"Country"}
        }
    }

    private fun handleNewParams(){
        if(isToInitiateNewSearch){
            isToInitiateNewSearch = false
            val check = mainViewModel.initiateNewSearch()
            clearAdapter(check)
        }
    }



    private fun observeInternetConnection() {

        mainViewModel.hasInternetConnection.observe(viewLifecycleOwner){

            bind.ivNoInternet.isVisible = !it


//            if(mainViewModel.isWaitingForNewSearch || mainViewModel.isWaitingForNewPage){
//                bind.ivNoInternet.isVisible = true
//            }

        }

//        mainViewModel.isServerNotResponding.observe(viewLifecycleOwner){
//            bind.ivNoInternet.isVisible = it
//        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        noResultMessage.isNoResultClickLogicSet = false
        isBindNoResultMessageInflated = false
        bind.rvSearchStations.adapter = null
        textLoadAnim = null
        _bind = null
        bindNoResultMessage = null
        isInitialLaunch = true
        isToHandleNewStationObserver = false
        isNewSearchForAnimations = true
        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            (activity as MainActivity).bind.progressBarBottom?.hide()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("CHECKTAGS", "calling search's on destroy")
    }

}





