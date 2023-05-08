package com.example.radioplayer.ui.fragments

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
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
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.*
import com.example.radioplayer.adapters.models.StationWithDateModel
import com.example.radioplayer.data.local.entities.HistoryDate
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.remote.entities.RadioStationsItem
import com.example.radioplayer.databinding.FragmentHistoryBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.example.radioplayer.ui.animations.objectSizeScaleAnimation
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY_ONE_DATE
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.example.radioplayer.utils.SpinnerExt
import com.example.radioplayer.utils.Utils
import com.example.radioplayer.utils.Utils.fromDateToString
import com.example.radioplayer.utils.addAction
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.sql.Date
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(
    FragmentHistoryBinding::inflate
) {


    private var dateForAdapters = ""

    private lateinit var datesAdapter : HistoryDatesAdapter

    @Inject
    lateinit var glide : RequestManager

    private val calendar = Calendar.getInstance()

    private var isInitialLoad = true

    private var stationsHistoryAdapter: PagingHistoryAdapter? = null

    private var titlesHistoryAdapter : TitleAdapter? = null

    @Inject
    lateinit var bookmarkedTitlesAdapter : BookmarkedTitlesAdapter

    private var isStationsAdapterSet = false
    private var isTitlesAdapterSet = false

    private var isToHandleNewStationObserver = false

    private var isBookmarkedTitlesObserverSet = false

    private val clipBoard : ClipboardManager? by lazy {
        ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
    }


    companion object{

       var isNewHistoryQuery = true
       var numberOfDates = 0

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        updateCurrentDate()

        setupRecyclerView()

        observePlaybackState()

        observeNewStation()

        setupAdapterClickListener()

        observeListOfDates()

//        setTvSelectDateClickListener()

        setSpinnerOpenCloseListener()

        setToolbar()

        setTitlesClickListener()

        setStationsClickListener()

        switchTitlesStationsUi(false)

        setBookmarksFabClickListener()

        bind.fabBookmarkedTitles.isVisible = !databaseViewModel.isHistoryInStationsTab

    }




    private fun setToolbar(){


        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            val color = ContextCompat.getColor(requireContext(), R.color.nav_bar_history_frag)
//            val colorStatus = ContextCompat.getColor(requireContext(), R.color.status_bar_history_frag)

            (activity as MainActivity).apply {
                window.navigationBarColor = color
                window.statusBarColor = color
            }

            bind.tvStationsUnSelected?.setOnClickListener {
                bind.tvStations.performClick()
            }
            bind.tvTitlesUnselected?.setOnClickListener {
                bind.tvTitles.performClick()
            }

            bind.viewSpinnerClickBox?.setOnTouchListener { v, event ->

                if (event.action == MotionEvent.ACTION_DOWN){
                    bind.tvSliderHeader.isPressed = true
                bind.spinnerDates.performClick()
                }

                true
            }

        } else {
            bind.viewToolbar.setBackgroundColor(Color.BLACK)
            bind.tvSliderHeader.setOnClickListener {
                bind.spinnerDates.performClick()
            }
        }
    }


    private fun setStationsAdapterLoadStateListener(){

        stationsHistoryAdapter?.addLoadStateListener {

            if (it.refresh is LoadState.Loading ||
                it.append is LoadState.Loading) { }
            else {

                if(isInitialLoad){
                    bind.rvHistory.apply {

                            Log.d("CHECKTAGS", "on layout")
                            scrollToPosition(0)
                            startLayoutAnimation()

                    }
                    isInitialLoad = false
                }
            }
        }
    }

    private fun setTitlesAdapterLoadStateListener(){

        titlesHistoryAdapter?.addLoadStateListener {

            if (it.refresh is LoadState.Loading ||
                it.append is LoadState.Loading) { }
            else {

                if(isInitialLoad){

                    bind.rvHistory.apply {

                            Log.d("CHECKTAGS", "child animate")
                            scrollToPosition(0)
                            startLayoutAnimation()

                    }
                    isInitialLoad = false
                }
            }
        }
    }



    private fun setRvLoadChildrenListener(){

        bind.rvHistory.addOnChildAttachStateChangeListener(
           object : RecyclerView.OnChildAttachStateChangeListener{
               override fun onChildViewAttachedToWindow(view: View) {

                   if(isInitialLoad){

                       bind.rvHistory.apply {

                               scrollToPosition(0)
                               startLayoutAnimation()

                       }
                       isInitialLoad = false
                   }

               }

               override fun onChildViewDetachedFromWindow(view: View) {

               }
           }
        )

    }


    private fun setSpinnerOpenCloseListener(){

        bind.spinnerDates.setSpinnerEventsListener( object : SpinnerExt.OnSpinnerEventsListener{
            override fun onSpinnerOpened(spinner: Spinner?) {

                bind.tvShrinkArrow?.
                    setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        R.drawable.ic_playlists_arrow_shrink,0)



            }

            override fun onSpinnerClosed(spinner: Spinner?) {
                bind.tvShrinkArrow?.
                setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.ic_playlists_arrow_expand,0)
            }
        })
    }






    private fun setupDatesSpinner(list : List<HistoryDate>){

        datesAdapter = HistoryDatesAdapter(list, requireContext())
        bind.spinnerDates.adapter = datesAdapter
        datesAdapter.selectedColor = ContextCompat.getColor(requireContext(), R.color.color_non_interactive)

    }




    private fun observeListOfDates(){

        databaseViewModel.listOfDates.observe(viewLifecycleOwner){

            numberOfDates = it.size

            val allHistory = HistoryDate("All dates", 0)

            val dates = it.toMutableList()
            dates.add(0, allHistory)

            setupDatesSpinner(dates)

            val pos = dates.indexOfFirst { historyDate ->
                historyDate.time == databaseViewModel.selectedDate
            }

            datesAdapter.selectedItemPosition = pos
            setSliderHeaderText(databaseViewModel.selectedDate)

            bind.spinnerDates.setSelection(pos)

//            if(pos <= 0) {
//                databaseViewModel.updateHistory.postValue(true)
//            } else {
//                databaseViewModel.updateHistory.postValue(false)
//            }


            setDatesSpinnerSelectListener()

            subscribeToHistory()

        }
    }


    private fun setDatesSpinnerSelectListener(){

        bind.spinnerDates.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                val item = datesAdapter.getItem(position) as HistoryDate

                if(databaseViewModel.selectedDate != item.time){

                    datesAdapter.selectedItemPosition = position

                    setSliderHeaderText(item.time)

                    databaseViewModel.selectedDate = item.time

                    loadHistory(true)
//
//                    if(position == 0){
//                        databaseViewModel.updateHistory.postValue(true)
//                        isNewHistoryQuery = true
//                    }
//                    else
//                        databaseViewModel.updateHistory.postValue(false)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }


    private fun loadHistory(isSameTab : Boolean = false){

        databaseViewModel.apply {

            if(!isHistoryInStationsTab && isSameTab){
                titlesHistoryAdapter?.submitData(lifecycle, PagingData.empty())
            }

            if(selectedDate == 0L){
                if(isHistoryInStationsTab){
                    getAllHistory()
                } else {
                    getAllTitles()
                }

            } else {
                if(isHistoryInStationsTab){
                    oneHistoryDateCaller.postValue(true)
                } else {
                    oneTitleDateCaller.postValue(true)
                }
            }
        }

        isInitialLoad = true
    }

    private fun setSliderHeaderText(time : Long){
        if(time == 0L) (bind.tvSliderHeader as TextView).text = "All dates"

        else{
            calendar.time = Date(time)

           val date = Utils.fromDateToStringShort(calendar)

            (bind.tvSliderHeader as TextView).text = date
        }
    }


    private fun observePlaybackState(){
        mainViewModel.playbackState.observe(viewLifecycleOwner){
            it?.let {

                when{
                    it.isPlaying -> {
                        stationsHistoryAdapter?.currentPlaybackState = true

                            stationsHistoryAdapter?.updateStationPlaybackState()

                    }
                    it.isPlayEnabled -> {
                        stationsHistoryAdapter?.currentPlaybackState = false

                            stationsHistoryAdapter?.updateStationPlaybackState()

                    }
                }
            }
        }
    }


    private fun observeNewStation(){

        RadioService.currentPlayingStation.observe(viewLifecycleOwner){ station ->

            if(databaseViewModel.isHistoryInStationsTab &&
                    RadioService.currentPlaylist != SEARCH_FROM_RECORDINGS){

                if(isToHandleNewStationObserver){

                    if(RadioService.currentPlaylist == SEARCH_FROM_HISTORY_ONE_DATE){
                        handleNewRadioStation(RadioService.currentPlayingItemPosition, station)

                    } else {

                        val index = stationsHistoryAdapter?.snapshot()?.items?.indexOfFirst {
                            it is StationWithDateModel.Station && it.radioStation.stationuuid == station.stationuuid
                        }

                        if(index != -1 && index != null){

                            handleNewRadioStation(index, station)
                        }

                    }
                } else {
                    isToHandleNewStationObserver = true
                }

            }

        }
    }


    private fun handleNewRadioStation(position : Int, station : RadioStation){

        bind.rvHistory.smoothScrollToPosition(position)

        bind.rvHistory.post {

            val holder = bind.rvHistory
                .findViewHolderForAdapterPosition(position)

            holder?.let {
                stationsHistoryAdapter?.updateOnStationChange(station, holder as PagingHistoryAdapter.StationViewHolder)
            }
        }
    }



    private fun setupRecyclerView (){


        bind.rvHistory.apply {


            layoutManager = LinearLayoutManager(requireContext())
            edgeEffectFactory = BounceEdgeEffectFactory()
            itemAnimator = null
            layoutAnimation = (activity as MainActivity).layoutAnimationController

            setRvLoadChildrenListener()

            if(databaseViewModel.isHistoryInStationsTab){
                setStationsHistoryAdapter()
            } else {

                if(databaseViewModel.isHistoryTitlesInBookmark){
                    switchToBookmarkedTitles()
                    bind.fabBookmarkedTitles.setImageResource(R.drawable.ic_bookmark_selected)
                } else {
                    setTitlesHistoryAdapter()
                }
            }
        }

    }

    private fun setStationsHistoryAdapter(){

        if(!isStationsAdapterSet){

            stationsHistoryAdapter = PagingHistoryAdapter(glide)

            setStationsAdapterLoadStateListener()

            stationsHistoryAdapter?.apply {

                currentDate = dateForAdapters
                defaultTextColor = ContextCompat.getColor(requireContext(), R.color.default_text_color)
                selectedTextColor = ContextCompat.getColor(requireContext(), R.color.selected_text_color)

                defaultSecondaryTextColor = ContextCompat.getColor(requireContext(), R.color.default_secondary_text_color)
                selectedSecondaryTextColor = ContextCompat.getColor(requireContext(), R.color.selected_secondary_text_color)

                alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
                titleSize = mainViewModel.stationsTitleSize

                separatorDefault = ContextCompat.getColor(requireContext(), R.color.station_bottom_separator_default)

                if(RadioService.currentPlaylist != SEARCH_FROM_RECORDINGS){
                    RadioService.currentPlayingStation.value?.let {
                        val id =  it.stationuuid
                        currentRadioStationID = id
                    }
                } else {
                    currentRadioStationID = ""
                }
            }

            databaseViewModel.setHistoryLiveData(viewLifecycleOwner.lifecycleScope)

            isStationsAdapterSet = true
        }


        bind.rvHistory.apply {
            adapter = stationsHistoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setTitlesHistoryAdapter(){

        if(!isTitlesAdapterSet){
            titlesHistoryAdapter = TitleAdapter(glide)
            setTitlesAdapterLoadStateListener()
            titlesHistoryAdapter?.apply {
                currentDate = dateForAdapters
                alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
                titleSize = mainViewModel.stationsTitleSize
                setOnClickListener { title ->
                  handleTitleClick(title.title)
                }

                onBookmarkClickListener { title ->

                    Toast.makeText(requireContext(), "Title bookmarked", Toast.LENGTH_SHORT).show()

                    databaseViewModel.upsertBookmarkedTitle(title)

                }

            }
            databaseViewModel.setTitlesLiveData(lifecycleScope)
            isTitlesAdapterSet = true
        }

        bind.rvHistory.apply {
            adapter = titlesHistoryAdapter
            setHasFixedSize(false)
        }
    }


    private fun handleTitleClick(title : String){

        val clip = ClipData.newPlainText("label", title)
        clipBoard?.setPrimaryClip(clip)

        Snackbar.make(
            requireActivity().findViewById(R.id.rootLayout),
            "Title copied", Snackbar.LENGTH_LONG).apply {

            setAction("WEBSEARCH"){

                val intent = Intent(Intent.ACTION_WEB_SEARCH)
                intent.putExtra(SearchManager.QUERY, title)
                startActivity(intent)


//                         val  browserIntent = Intent(Intent.ACTION_VIEW,
//                             Uri.parse("https://soundcloud.com/search?q=" + title.title))
//                            browserIntent.putExtra(SearchManager.QUERY, title.title)
//                            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            startActivity(browserIntent)

            }
            addAction(R.layout.snackbar_extra_action, "YOUTUBE"){
                val intent = Intent(Intent.ACTION_SEARCH)
                intent.setPackage("com.google.android.youtube")
                intent.putExtra("query", title)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }.show()


    }

    private fun setTitlesClickListener(){

        bind.tvTitles.setOnClickListener {

            if(databaseViewModel.isHistoryInStationsTab){

                bind.fabBookmarkedTitles.isVisible = true
                isToHandleNewStationObserver = false
                databaseViewModel.isHistoryInStationsTab = false

                if(databaseViewModel.isHistoryTitlesInBookmark){
                    switchToBookmarkedTitles()

                } else {
                    handleSwitchToNotMarkedTitles()
                }

            }

            switchTitlesStationsUi(true)

        }
    }


    private fun handleSwitchToNotMarkedTitles(){
        setTitlesHistoryAdapter()

        if(databaseViewModel.selectedDate != 0L){
            titlesHistoryAdapter?.submitData(lifecycle, PagingData.empty())
        }

        loadHistory()
    }

    private fun setBookmarksFabClickListener(){

        bind.fabBookmarkedTitles.apply {

            setOnClickListener {

                databaseViewModel.isHistoryTitlesInBookmark = !databaseViewModel.isHistoryTitlesInBookmark

                if(databaseViewModel.isHistoryTitlesInBookmark){
                    setImageResource(R.drawable.ic_bookmark_selected)
                    switchToBookmarkedTitles()
                    isToHandleNewStationObserver = false
                } else {
                    setImageResource(R.drawable.ic_bookmark_hollow)
                    handleSwitchToNotMarkedTitles()
                }
            }
        }
    }



    private fun switchToBookmarkedTitles(){

        bind.rvHistory.apply {
            adapter = bookmarkedTitlesAdapter
            setHasFixedSize(false)

        }

        if(!isBookmarkedTitlesObserverSet){

            isBookmarkedTitlesObserverSet = true

            bookmarkedTitlesAdapter.apply {
                alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
                titleSize = mainViewModel.stationsTitleSize

                setOnClickListener { title ->
                    handleTitleClick(title.title)
                }

            }

            databaseViewModel.bookmarkedTitlesLivedata.observe(viewLifecycleOwner){

                bookmarkedTitlesAdapter.listOfTitles = it

            }

            bind.rvHistory.post {
                bind.rvHistory.scheduleLayoutAnimation()
            }

        } else {
            bind.rvHistory.scheduleLayoutAnimation()
        }

    }




    private fun setStationsClickListener(){

        bind.tvStations.setOnClickListener {

            if(!databaseViewModel.isHistoryInStationsTab){

                bind.fabBookmarkedTitles.isVisible = false

                setStationsHistoryAdapter()

                if(databaseViewModel.selectedDate != 0L){
                    stationsHistoryAdapter?.submitData(lifecycle, PagingData.empty())
                }

                databaseViewModel.isHistoryInStationsTab = true

                switchTitlesStationsUi(true)

                loadHistory()
            }
        }
    }

    private fun switchTitlesStationsUi(isToAnimate : Boolean){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
            if(databaseViewModel.isHistoryInStationsTab){
                bind.tvStations.setTextAppearance(R.style.selectedTitle)
                bind.tvTitles.setTextAppearance(R.style.unselectedTitle)

                if(isToAnimate){
                    bind.tvStations.objectSizeScaleAnimation(15f, 18f)
                    bind.tvTitles.objectSizeScaleAnimation(18f, 15f)
                }

            } else {
                bind.tvStations.setTextAppearance(R.style.unselectedTitle)
                bind.tvTitles.setTextAppearance(R.style.selectedTitle)

                if(isToAnimate){
                    bind.tvStations.objectSizeScaleAnimation(18f, 15f)
                    bind.tvTitles.objectSizeScaleAnimation(15f, 18f)
                }

            }
        } else {

            if(databaseViewModel.isHistoryInStationsTab){
                bind.viewToolbar.setBackgroundResource(R.drawable.toolbar_history_stations_protected)
                bind.tvStationsUnSelected?.visibility = View.INVISIBLE
                bind.tvTitlesUnselected?.visibility = View.VISIBLE

            } else {
                bind.viewToolbar.setBackgroundResource(R.drawable.toolbar_history_titles_protected)


                bind.tvStationsUnSelected?.visibility = View.VISIBLE
                bind.tvTitlesUnselected?.visibility = View.INVISIBLE
            }
        }
    }



    private fun subscribeToHistory(){

            databaseViewModel.observableHistory.observe(viewLifecycleOwner){

                stationsHistoryAdapter?.submitData(lifecycle, it)
            }


        databaseViewModel.observableTitles.observe(viewLifecycleOwner){

            titlesHistoryAdapter?.submitData(lifecycle, it)

        }

//            databaseViewModel.historyFlow.collectLatest {
//
//                Log.d("CHECKTAGS", "collect")
//
//
//                historyAdapter.submitData(it)
//
//            }

    }

    private fun setupAdapterClickListener(){

        stationsHistoryAdapter?.setOnClickListener { station, position ->

            val flag = if(databaseViewModel.selectedDate == 0L){
                SEARCH_FROM_HISTORY
            } else
                SEARCH_FROM_HISTORY_ONE_DATE

            mainViewModel.playOrToggleStation(station, flag,
                itemIndex = position, historyItemId = station.stationuuid)
            databaseViewModel.checkDateAndUpdateHistory(station.stationuuid)

        }
    }


    private fun updateCurrentDate(){

        val time = System.currentTimeMillis()
        calendar.time = Date(time)
        val parsedDate = fromDateToString(calendar)
        dateForAdapters =  parsedDate

    }

    override fun onDestroyView() {
        super.onDestroyView()

        databaseViewModel.cleanHistory()
        isNewHistoryQuery = true
        isInitialLoad = true
        isStationsAdapterSet = false
        isTitlesAdapterSet = false
        bind.rvHistory.adapter = null
        stationsHistoryAdapter = null
        titlesHistoryAdapter = null
        _bind = null
        isToHandleNewStationObserver = false
        isBookmarkedTitlesObserverSet = false
    }

}