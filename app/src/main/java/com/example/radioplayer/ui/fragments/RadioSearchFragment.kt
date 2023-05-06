package com.example.radioplayer.ui.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PagingRadioAdapter
import com.example.radioplayer.adapters.RadioDatabaseAdapter
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

        setAdapterOnClickListener()

        setOnRefreshSearch()

        listenSearchButton()

        setDragListenerForLayout()
        setDragListenerForButton()
        getFabSearchPositionIfNeeded()

        observeNoResultDetector()

        setToolbar()

        setSearchParamsFabClickListener()

        observeInternetConnection()

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            textLoadAnim = TextLoadAnim(
                requireContext(), bind.tvLoading!!)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun setSearchParamsFabClickListener(){

        bind.fabSearchOrder.setOnClickListener {
            SearchParamsDialog(requireContext(), mainViewModel).show()
        }

    }



    private fun setToolbar(){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            bind.viewToolbar.setBackgroundResource(R.drawable.toolbar_search_vector)
            val color = ContextCompat.getColor(requireContext(), R.color.nav_bar_search_fragment)
//            val statusBar = ContextCompat.getColor(requireContext(), R.color.status_bar_search_fragment)

            (activity as MainActivity).apply {
                window.navigationBarColor = color
                window.statusBarColor = color
            }

//            (bind.tvTag as TextViewOutlined).apply {
//                setColors(
//                    ContextCompat.getColor(requireContext(), R.color.text_button_search_tag)
//                )
//                setStrokeWidth(3.5f)
//            }
//
//            (bind.tvName as TextViewOutlined).apply {
//                setColors(
//                    ContextCompat.getColor(requireContext(), R.color.text_button_search_name)
//                )
//                setStrokeWidth(3.5f)
//            }
//
//            (bind.tvSelectedCountry as TextViewOutlined).apply {
//                setColors(
//                    ContextCompat.getColor(requireContext(), R.color.text_button_search_country)
//                )
//                setStrokeWidth(3.5f)
//            }



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

        if(isToHandleNewStationObserver){

            if(RadioService.currentPlaylist == SEARCH_FROM_API ){

                handleNewRadioStation(RadioService.currentPlayingItemPosition, station)

            } else {

                val index = pagingRadioAdapter.snapshot().items
                    .indexOfFirst {
                        it.stationuuid == station.stationuuid
                    }

                if(index != -1){
                    handleNewRadioStation(index, station)
                }
            }
        } else {
            isToHandleNewStationObserver = true
        }

        }
    }

    private fun handleNewRadioStation(position : Int, station : RadioStation){

        bind.rvSearchStations.smoothScrollToPosition(position)

        bind.rvSearchStations.post {

            val holder = bind.rvSearchStations
                .findViewHolderForAdapterPosition(position)

            holder?.let {
                pagingRadioAdapter.updateOnStationChange(station, holder as PagingRadioAdapter.RadioItemHolder)
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

            mainViewModel.playOrToggleStation(station, SEARCH_FROM_API, itemIndex = index)

            databaseViewModel.insertRadioStation(station)
            databaseViewModel.checkDateAndUpdateHistory(station.stationuuid)

            Log.d("CHECKTAGS", station.url.toString())



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


            RadioService.currentPlayingStation.value?.let {
              val id =  it.stationuuid
                pagingRadioAdapter.currentRadioStationId = id
            }
        }
    }

    private fun setAdapterLoadStateListener(){

        pagingRadioAdapter.addLoadStateListener {

            if (it.refresh is LoadState.Loading ||
                it.append is LoadState.Loading)

            {

                if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
                    (activity as MainActivity).startSeparatorsLoadAnim()
                } else {

                    Log.d("CHECKTAGS", isNewSearchForAnimations.toString())

                    if(!isNewSearchForAnimations)
                        (activity as MainActivity).bind.progressBarBottom?.show()
                    else
                        (activity as MainActivity).bind.progressBarBottom?.hide()
//                    bind.progressBar?.show()
//                    else
//                    bind.progressBar?.hide()
                }



            }

            else {

                if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
                    (activity as MainActivity).endSeparatorsLoadAnim()
                } else {
                    textLoadAnim?.endLoadingAnim()
                    (activity as MainActivity).bind.progressBarBottom?.hide()
                }


                if(isNewSearchForAnimations ){

                    bind.rvSearchStations.apply {
                        if(isInitialLaunch){
                            scheduleLayoutAnimation()
                            isInitialLaunch = false

                        } else {
                            scrollToPosition(0)
                            startLayoutAnimation()
                        }
                    }
                    isNewSearchForAnimations = false
                    isToShowLoadingMessage = false
                }
            }
        }
    }



    private fun subscribeToStationsFlow(){

        viewLifecycleOwner.lifecycleScope.launch {

            mainViewModel.stationsFlow.collectLatest {

                isNewSearchForAnimations = true

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
           TagPickerDialog(requireContext(), mainViewModel).show()
        }



        bind.tvName.setOnClickListener {

            if(mainViewModel.isNameAutoSearch){
                NameAutoDialog(requireContext(), mainViewModel).show()
            } else {
                NameDialog(requireContext(), mainViewModel).show()
            }

        }

        bind.tvSelectedCountry.setOnClickListener {

            CountryPickerDialog(requireContext(), mainViewModel).show()

        }
    }


    private fun setOnRefreshSearch(){

        bind.swipeRefresh.setOnRefreshListener {

           val check = mainViewModel.initiateNewSearch()

            bind.swipeRefresh.isRefreshing = false

            clearAdapter(check)
        }
    }

    private fun listenSearchButton(){

        bind.fabInitiateSearch.setOnClickListener {
          val check =  mainViewModel.initiateNewSearch()
            clearAdapter(check)
        }
    }

    private fun clearAdapter(check : Boolean){
        if(check){
            isToShowLoadingMessage = true
            pagingRadioAdapter.submitData(lifecycle, PagingData.empty())
        }
    }


    private fun setSearchParamsObservers(){

        mainViewModel.searchParamTag.observe(viewLifecycleOwner){

            (bind.tvTag as TextView).text  = it.ifBlank { "Tag" }

        }

        mainViewModel.searchParamName.observe(viewLifecycleOwner){

            (bind.tvName as TextView).text = it.ifBlank { "Name" }

            if(mainViewModel.isNameAutoSearch){

               val check = mainViewModel.initiateNewSearch()
               clearAdapter(check)
            }
        }

        mainViewModel.searchParamCountry.observe(viewLifecycleOwner){


            (bind.tvSelectedCountry as TextView).text = it.ifBlank {"Country"}

        }
    }


    private fun observeInternetConnection() {

        mainViewModel.hasInternetConnection.observe(viewLifecycleOwner){
            bind.ivNoInternet.isVisible = !it
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        bind.rvSearchStations.adapter = null
        textLoadAnim = null
        _bind = null
        isInitialLaunch = true
        isToHandleNewStationObserver = false
    }

}





