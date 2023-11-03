package com.onlyradio.radioplayer.ui.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.adapters.PagingRadioAdapter
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.databinding.FragmentRadioSearchBinding
import com.onlyradio.radioplayer.databinding.StubNoResultMessageBinding
import com.onlyradio.radioplayer.domain.PlayingStationState
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.extensions.makeToast
import com.onlyradio.radioplayer.extensions.observeFlow
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.ui.animations.TextLoadAnim
import com.onlyradio.radioplayer.ui.animations.slideAnim
import com.onlyradio.radioplayer.ui.dialogs.CountryPickerDialog
import com.onlyradio.radioplayer.ui.dialogs.NameAutoDialog
import com.onlyradio.radioplayer.ui.dialogs.SearchParamsDialog
import com.onlyradio.radioplayer.ui.dialogs.TagPickerDialog
import com.onlyradio.radioplayer.ui.stubs.NoResultMessage
import com.onlyradio.radioplayer.ui.viewmodels.TAB_STATIONS
import com.onlyradio.radioplayer.utils.Constants.CLICK_DEBOUNCE
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_API
import com.onlyradio.radioplayer.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class RadioSearchFragment : BaseFragment<FragmentRadioSearchBinding>(
    FragmentRadioSearchBinding::inflate
) {


    private var isNewSearchForAnimations = true

    private var isInitialLaunch = true

    @Inject
    lateinit var pagingRadioAdapter : PagingRadioAdapter

    @Inject
    lateinit var glide : RequestManager

    private var textLoadAnim : TextLoadAnim? = null

    private var isToHandleNewStationObserver = false

    private var isToInitiateNewSearch = false

    private var bindNoResultMessage : StubNoResultMessageBinding? = null

    private var isBindNoResultMessageInflated = false

    private var noResultMessage = NoResultMessage(
        postInitiateNewSearch = {isToInitiateNewSearch = true},
        initiateNewSearch = { initiateNewSearch() }
    )

    private var stationClickJob : Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.stubTvNoResultMessage.setOnInflateListener{ _, bindView ->
            bindNoResultMessage = StubNoResultMessageBinding.bind(bindView)
        }

        setSearchParamsObservers()

        setSearchToolbar()

        setRecycleView()

        subscribeToStationsFlow()

        observeStationWithPlayback()

//        observePlaybackState()
//
//        observeNewStation()

        setRecyclerViewAttachChildrenListener()

        setAdapterOnClickListener()

        setOnRefreshSearch()

        setSearchButton()

        observeNoResultDetector()

        setToolbar()

        setSearchParamsFabClickListener()

        observeInternetConnection()

        observeSearchState()

        observeIsToShowLoadingMessage()

        searchDialogsViewModels.checkTagsLastUpdate()

//
//        textLoadAnim = TextLoadAnim(requireContext(), bind.tvLoading)

    }

    private fun observeIsToShowLoadingMessage(){

        textLoadAnim = TextLoadAnim(requireContext(), bind.tvLoading)

        mainViewModel.isToShowLoadingMessage.observe(viewLifecycleOwner){
            if(it){
                textLoadAnim?.startLoadingAnim()
            } else {
                textLoadAnim?.endLoadingAnim()
            }
        }
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


    private fun observeStationWithPlayback(){

        observeFlow(mainViewModel.isPlayingFlow.combine(RadioService.currentPlayingStation.asFlow()){ isPlaying, station ->
            PlayingStationState(station?.stationuuid ?: "", isPlaying)
        }){ state ->
            if(RadioService.isFromRecording) return@observeFlow

            val id = state.stationId
            val index = getCurrentItemPosition(id)

            if(!isToHandleNewStationObserver){
                isToHandleNewStationObserver = true
                pagingRadioAdapter.updateSelectedItemValues(index, id, state.isPlaying)
                return@observeFlow
            }


            if(index >= 0){
                handleNewRadioStation(index, id, state.isPlaying)
            }
        }
    }


//    private fun observePlaybackState(){
//        mainViewModel.isPlaying.observe(viewLifecycleOwner){ isPlaying ->
//
//            if(!RadioService.isFromRecording){
//                if(pagingRadioAdapter.onPlaybackState(isPlaying)){
//                    pagingRadioAdapter.updateStationPlaybackState()
//                }
//            }
//        }
//    }
//
//    private fun observeNewStation(){
//
//        RadioService.currentPlayingStation.observe(viewLifecycleOwner){ station ->
//
//            val index = getCurrentItemPosition(station.stationuuid)
//            val id = station.stationuuid
//
//            if(isToHandleNewStationObserver){
//
//                if(index != -1){
//                    handleNewRadioStation(index, id, false)
//                }
//            }
//            else {
//                isToHandleNewStationObserver = true
//                pagingRadioAdapter.updateSelectedItemValues(index, id)
//            }
//        }
//    }


    private fun getCurrentItemPosition(stationId : String?) : Int {

        return if(RadioService.currentMediaItems == SEARCH_FROM_API )

            mainViewModel.getPlayerCurrentIndex()

         else

            pagingRadioAdapter.snapshot().items
                .indexOfFirst {
                    it.stationuuid == (stationId
                        ?: RadioService.currentPlayingStation.value?.stationuuid)

        }

    }


    private fun handleNewRadioStation(position : Int, stationId : String, isPlaying: Boolean){

        bind.rvSearchStations.apply {

            post {
                smoothScrollToPosition(position)

                post {

                    pagingRadioAdapter.onNewPlayingItem(position, stationId, isPlaying)

                }
            }
        }
    }




    private fun setAdapterOnClickListener(){

        pagingRadioAdapter.setOnClickListener { station, index ->

            if(stationClickJob?.isActive == true)
                return@setOnClickListener

            stationClickJob = lifecycleScope.launch {
                val isToChangeMediaItems = RadioService.currentMediaItems != SEARCH_FROM_API

                mainViewModel.playOrToggleStation(station.stationuuid, SEARCH_FROM_API,
                    itemIndex = index, isToChangeMediaItems = isToChangeMediaItems)

                delay(CLICK_DEBOUNCE)
            }
        }
    }


    private fun setRecycleView(){

        bind.rvSearchStations.apply {

            adapter = pagingRadioAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)

            pagingRadioAdapter.initialiseValues(requireContext(), settingsViewModel.stationsTitleSize)


//            itemAnimator = null

            layoutAnimation = (activity as MainActivity).layoutAnimationController

        }
    }

    private fun observeSearchState(){

        mainViewModel.searchLoadingState.observe(viewLifecycleOwner){

            mainViewModel.updateIsToPlayLoadAnim(it)

        }
    }



    private fun setRecyclerViewAttachChildrenListener(){
        bind.rvSearchStations.addOnChildAttachStateChangeListener(
            object : RecyclerView.OnChildAttachStateChangeListener{
                override fun onChildViewAttachedToWindow(view: View) {

                    if(isNewSearchForAnimations){

                        isNewSearchForAnimations = false

                        bind.rvSearchStations.apply {
                            if(isInitialLaunch){

                                post {
                                    val index = getCurrentItemPosition(null)

                                    if(index != -1 ) scrollToPosition(index)

                                    startLayoutAnimation()
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

                    pagingRadioAdapter.submitData(it)

                }
            }
        }
    }


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
        }

        bind.tvSelectedCountry.setOnClickListener {

            CountryPickerDialog(
                requireContext = requireContext(),
                searchDialogsViewModel = searchDialogsViewModels,
                glide = glide)
            { countryCode, countryName ->
                if(mainViewModel.isFullAutoSearch)
                    isToInitiateNewSearch = true

                mainViewModel.updateCountrySearchSelection(countryCode, countryName)
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
                    requireContext().makeToast(R.string.same_search_query)
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
              requireContext().makeToast(R.string.same_search_query)
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

//            pagingRadioAdapter.previousItemHolder = null

        }
    }


    private fun setSearchParamsObservers(){

        mainViewModel.searchParamTag.observe(viewLifecycleOwner){

            handleNewParams()

            (bind.tvTag as TextView).text  = it.ifBlank {
                requireContext().resources.getString(R.string.Tag)
            }
        }

        mainViewModel.searchParamName.observe(viewLifecycleOwner){

            if(noResultMessage.isNoResultClick){
                noResultMessage.isNoResultClick = false
                handleNewParams()
            }

            (bind.tvName as TextView).text = it.ifBlank {
                requireContext().resources.getString(R.string.Name)
            }

        }

        mainViewModel.searchParamCountry.observe(viewLifecycleOwner){

            handleNewParams()

            (bind.tvSelectedCountry as TextView).text = it.ifBlank {
                requireContext().resources.getString(R.string.Country)
            }
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

    }


}





