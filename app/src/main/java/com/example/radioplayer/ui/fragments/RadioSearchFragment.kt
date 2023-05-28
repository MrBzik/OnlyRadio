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
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
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
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.TextLoadAnim
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.dialogs.*
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


    companion object {

        var tagAdapterPosition : Parcelable? = null
        var countriesAdapterPosition : Parcelable? = null


        val tagsList : ArrayList<TagWithGenre> by lazy { ArrayList<TagWithGenre>().apply {

                add(TagWithGenre.Genre(TAG_BY_PERIOD))
                addAll(tagsListByPeriod)

                add(TagWithGenre.Genre(TAG_BY_SPECIAL))
                addAll(tagsListSpecial)

                add(TagWithGenre.Genre(TAG_BY_GENRE))
                addAll(tagsListByGenre)

                add(TagWithGenre.Genre(TAG_BY_SUB_GENRE))
                addAll(tagsListBySubGenre)

                add(TagWithGenre.Genre(TAG_BY_CLASSIC))
                addAll(tagsListClassics)

                add(TagWithGenre.Genre(TAG_BY_MINDFUL))
                addAll(tagsListMindful)

                add(TagWithGenre.Genre(TAG_BY_EXPERIMENTAL))
                addAll(tagsListExperimental)

                add(TagWithGenre.Genre(TAG_BY_TALK))
                addAll(tagsListByTalk)

                add(TagWithGenre.Genre(TAG_BY_RELIGION))
                addAll(tagsListReligion)

                add(TagWithGenre.Genre(TAG_BY_ORIGIN))
                addAll(tagsListByOrigin)

                add(TagWithGenre.Genre(TAG_BY_OTHER))
                addAll(tagsListOther)
            }
        }

        val listOfCountries : ArrayList<CountryWithRegion> by lazy{
            ArrayList<CountryWithRegion>().apply {

                add(CountryWithRegion.Region(COUNTRY_REGION_AFRICA))
                addAll(listOfAfrica)
                 add(CountryWithRegion.Region(COUNTRY_REGION_ASIA))
                addAll(listOfAsia)
                 add(CountryWithRegion.Region(COUNTRY_REGION_CENTRAL_AMERICA))
                addAll(listOfCentralAmerica)
                 add(CountryWithRegion.Region(COUNTRY_REGION_NORTH_AMERICA))
                addAll(listOfNorthAmerica)
                 add(CountryWithRegion.Region(COUNTRY_REGION_SOUTH_AMERICA))
                addAll(listOfSouthAmerica)
                 add(CountryWithRegion.Region(COUNTRY_REGION_EAST_EUROPE))
                addAll(listOfEastEurope)
                 add(CountryWithRegion.Region(COUNTRY_REGION_WEST_EUROPE))
                addAll(listOfWestEurope)
                 add(CountryWithRegion.Region(COUNTRY_REGION_MIDDLE_EAST))
                addAll(listOfMiddleEast)
                 add(CountryWithRegion.Region(COUNTRY_REGION_OCEANIA))
                addAll(listOfOceania)
                 add(CountryWithRegion.Region(COUNTRY_REGION_THE_CARIBBEAN))
                addAll(listOfTheCaribbean)

            }
        }


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSearchParamsObservers()

        setSearchToolbar()

        setRecycleView()

        subscribeToStationsFlow()

        observePlaybackState()

        observeNewStation()

        setAdapterLoadStateListener()

        setRecyclerViewAttachChildrenListener()

        setAdapterOnClickListener()

        setOnRefreshSearch()

        setSearchButton()

        observeNoResultDetector()

        setToolbar()

        setSearchParamsFabClickListener()

        observeInternetConnection()

        observeSearchState()


        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            textLoadAnim = TextLoadAnim(
                requireContext(), bind.tvLoading!!)
        }
    }


    private fun setSearchButton(){

        if(!mainViewModel.isFullAutoSearch){
            setDragListenerForLayout()
            setDragListenerForButton()
            getFabSearchPositionIfNeeded()
            listenSearchButton()
        }
    }



    private fun setSearchParamsFabClickListener(){

        bind.fabSearchOrder.setOnClickListener {
            SearchParamsDialog(requireContext(), mainViewModel){
                if(mainViewModel.isFullAutoSearch){
                    val check = mainViewModel.initiateNewSearch()
                    clearAdapter(check)
                }
            }.show()
        }

    }



    private fun setToolbar(){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            bind.viewToolbar.setBackgroundResource(R.drawable.toolbar_search_vector)
            val color = ContextCompat.getColor(requireContext(), R.color.nav_bar_search_fragment)

            (activity as MainActivity).apply {
                window.navigationBarColor = color
                window.statusBarColor = color
            }

        }

        else {
            bind.viewToolbar.setBackgroundColor(Color.BLACK)
        }
    }


    private fun observeNoResultDetector(){

        mainViewModel.noResultDetection.observe(viewLifecycleOwner){noResult ->

            if(noResult){

                val tagExact = if(mainViewModel.isTagExact) "(Exact)" else ""
                val nameExact = if(mainViewModel.isNameExact) "(Exact)" else ""

                val tag = if(mainViewModel.lastSearchTag.isBlank()) ""
                else "tag $tagExact: ${mainViewModel.lastSearchTag}\n\n"

                val name = if(mainViewModel.lastSearchName.isBlank()) ""
                else "name $nameExact: ${mainViewModel.lastSearchName}\n\n"

                val country = if(mainViewModel.searchFullCountryName.isBlank()) ""
                else "country: ${mainViewModel.searchFullCountryName}\n\n"

                val language = if(!mainViewModel.isSearchFilterLanguage) ""
                else "Language: ${Locale.getDefault().displayLanguage}\n\n"

                val bitrateMin = if(mainViewModel.minBitrateOld == BITRATE_0) ""
                else "Min bitrate: ${mainViewModel.minBitrateOld} kbps\n\n"

                val bitrateMax = if(mainViewModel.maxBitrateOld == BITRATE_MAX) ""
                else "Max bitrate: ${mainViewModel.maxBitrateOld} kbps"

                val message = "No results for\n\n\n$tag$name$country$language$bitrateMin$bitrateMax"

                bind.tvResultMessage.visibility = View.VISIBLE

                bind.tvResultMessage.text = message


            }   else bind.tvResultMessage.visibility = View.INVISIBLE
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
                            pagingRadioAdapter.currentPlaybackState = true

                            pagingRadioAdapter.updateStationPlaybackState()

                        }
                        it.isPlayEnabled -> {
                            pagingRadioAdapter.currentPlaybackState = false

                            pagingRadioAdapter.updateStationPlaybackState()
                        }
                    }
                }
            }
        }
    }



    private fun getFabSearchPositionIfNeeded(){
        bind.fabInitiateSearch.doOnLayout {
            if (mainViewModel.isFabMoved || mainViewModel.isFabUpdated) {
                bind.fabInitiateSearch.x = mainViewModel.fabX
                bind.fabInitiateSearch.y = mainViewModel.fabY
            }
            bind.fabInitiateSearch.isVisible = true
        }
    }

    private fun setDragListenerForLayout(){
            var tempX = 0f
            var tempY = 0f

        bind.root.setOnDragListener { v, event ->
            when(event.action){
                DragEvent.ACTION_DRAG_LOCATION -> {
                   tempX = event.x
                   tempY = event.y
                }
                DragEvent.ACTION_DRAG_ENDED ->{

                    bind.fabInitiateSearch.x = tempX - bind.fabInitiateSearch.width/2
                    bind.fabInitiateSearch.y = tempY - bind.fabInitiateSearch.height/2

                    mainViewModel.fabX = bind.fabInitiateSearch.x
                    mainViewModel.fabY = bind.fabInitiateSearch.y
                    mainViewModel.isFabUpdated = true
                }
            }
            true
        }
    }

    private fun setDragListenerForButton(){
        bind.fabInitiateSearch.setOnLongClickListener { view ->
            val shadow = View.DragShadowBuilder(bind.fabInitiateSearch)
            view.startDragAndDrop(null, shadow, view, 0)
            true
        }
    }


    private fun setAdapterOnClickListener(){

        pagingRadioAdapter.setOnClickListener { station, index ->


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
            pagingRadioAdapter.apply {
                defaultTextColor = ContextCompat.getColor(requireContext(), R.color.default_text_color)
                selectedTextColor = ContextCompat.getColor(requireContext(), R.color.selected_text_color)

                defaultSecondaryTextColor = ContextCompat.getColor(requireContext(), R.color.default_secondary_text_color)
                selectedSecondaryTextColor = ContextCompat.getColor(requireContext(), R.color.selected_secondary_text_color)

                alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
                titleSize = mainViewModel.stationsTitleSize
                separatorDefault = ContextCompat.getColor(requireContext(), R.color.station_bottom_separator_default)

            }

            itemAnimator = null

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
                    textLoadAnim?.endLoadingAnim()
                    (activity as MainActivity).bind.progressBarBottom?.hide()
                }
            }
        }
    }



    private fun setAdapterLoadStateListener(){

        pagingRadioAdapter.addLoadStateListener {

            if (it.refresh is LoadState.Loading ||
                it.append is LoadState.Loading)

            {
//                if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
//                    (activity as MainActivity).startSeparatorsLoadAnim()
//                } else {
//
//                    if(!isNewSearchForAnimations)
//                        (activity as MainActivity).bind.progressBarBottom?.show()
//                    else
//                        (activity as MainActivity).bind.progressBarBottom?.hide()
//                }
            }

            else {

//                if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
//                    (activity as MainActivity).endSeparatorsLoadAnim()
//
//                } else {
//                    textLoadAnim?.endLoadingAnim()
//                    (activity as MainActivity).bind.progressBarBottom?.hide()
//                }


//                if(mainViewModel.isWaitingForNewSearch)
//                    pagingRadioAdapter.submitData(lifecycle, PagingData.empty())


//                if(isNewSearchForAnimations){
//
//                    isNewSearchForAnimations = false
//                    isToShowLoadingMessage = false
//
//                    Log.d("CHECKTAGS", "is adapter load state listener")
//
//                    bind.rvSearchStations.apply {
//                        if(isInitialLaunch){
//
//                            if(RadioService.currentPlayingItemPosition == -1){
//                                startLayoutAnimation()
//                            } else {
//
//                                post {
//                                    val index = getCurrentItemPosition(null)
//
//                                    if(index != -1 ) scrollToPosition(index)
//
//                                    startLayoutAnimation()
//                                }
//
//                            }
//
//                            isInitialLaunch = false
//
//                        } else {
//                            scrollToPosition(0)
//                            startLayoutAnimation()
//                        }
//                    }
//
//                }
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

                        Log.d("CHECKTAGS", "is adapter load state listener")

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

            mainViewModel.stationsFlow.collectLatest {

                Log.d("CHECKTAGS", "collecting latest")

//                if(!isInitialLaunch){
//                    launchRecyclerOutAnim()
//                }

                if(isToShowLoadingMessage || mainViewModel.isInitialLaunchOfTheApp){
                    mainViewModel.isInitialLaunchOfTheApp = false

                    if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES)
                        showLoadingResultsMessage()
                    else {
                        bind.tvResultMessage.visibility = View.INVISIBLE
                        textLoadAnim?.startLoadingAnim()
                    }
                }

                pagingRadioAdapter.submitData(it)

            }
        }
    }



    private fun showLoadingResultsMessage(){
        bind.tvResultMessage.apply {
            visibility = View.VISIBLE
            text = "Waiting for response from servers..."
            slideAnim(100, 0, R.anim.fade_in_anim)
        }
    }


    private fun setSearchToolbar() {

        bind.tvTag.setOnClickListener {
           TagPickerDialog(requireContext(), mainViewModel){
               if(mainViewModel.isFullAutoSearch)
                   isToInitiateNewSearch = true
           }.show()
        }



        bind.tvName.setOnClickListener {

            if(mainViewModel.isNameAutoSearch){
                NameAutoDialog(requireContext(), mainViewModel){

                    val check = mainViewModel.initiateNewSearch()
                    clearAdapter(check)

                }.show()
            } else {
                NameDialog(requireContext(), mainViewModel){
                    if(mainViewModel.isFullAutoSearch)
                        isToInitiateNewSearch = true
                }.show()
            }

        }

        bind.tvSelectedCountry.setOnClickListener {

            CountryPickerDialog(requireContext(), mainViewModel){
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
            Log.d("CHECKTAGS", "is clearing adapter")

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

            handleNewParams()

            (bind.tvName as TextView).text = it.ifBlank { "Name" }

        }

        mainViewModel.searchParamCountry.observe(viewLifecycleOwner){

            handleNewParams()

            (bind.tvSelectedCountry as TextView).text = it.ifBlank {"Country"}

            bind.fabInitiateSearch
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
        bind.rvSearchStations.adapter = null
        textLoadAnim = null
        _bind = null
        isInitialLaunch = true
        isToHandleNewStationObserver = false
        isNewSearchForAnimations = true
        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            (activity as MainActivity).bind.progressBarBottom?.hide()
        }
    }
}





