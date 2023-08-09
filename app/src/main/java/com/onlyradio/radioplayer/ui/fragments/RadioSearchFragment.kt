package com.onlyradio.radioplayer.ui.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.adapters.PagingRadioAdapter
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.databinding.FragmentRadioSearchBinding
import com.onlyradio.radioplayer.databinding.StubNoResultMessageBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.exoPlayer.isPlayEnabled
import com.onlyradio.radioplayer.exoPlayer.isPlaying
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.ui.animations.TextLoadAnim
import com.onlyradio.radioplayer.ui.animations.slideAnim
import com.onlyradio.radioplayer.ui.dialogs.CountryPickerDialog
import com.onlyradio.radioplayer.ui.dialogs.NameAutoDialog
import com.onlyradio.radioplayer.ui.dialogs.SearchParamsDialog
import com.onlyradio.radioplayer.ui.dialogs.TagPickerDialog
import com.onlyradio.radioplayer.ui.stubs.NoResultMessage
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_API
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

    private var textLoadAnim : TextLoadAnim? = null

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


    private fun observeNewStation(){

        RadioService.currentPlayingStation.observe(viewLifecycleOwner){ station ->

//        if(RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS){

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
//        }
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

            pagingRadioAdapter.utils.initialiseValues(requireContext(), settingsViewModel.stationsTitleSize)


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

            pagingRadioAdapter.previousItemHolder = null

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

    }


}





