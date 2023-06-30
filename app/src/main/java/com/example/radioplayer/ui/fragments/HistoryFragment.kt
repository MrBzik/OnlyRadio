package com.example.radioplayer.ui.fragments

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.*
import com.example.radioplayer.adapters.models.StationWithDateModel
import com.example.radioplayer.adapters.models.TitleWithDateModel
import com.example.radioplayer.data.local.entities.BookmarkedTitle
import com.example.radioplayer.data.local.entities.HistoryDate
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.FragmentHistoryBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.AdapterAnimator
import com.example.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.example.radioplayer.ui.animations.SwapTitlesUi
import com.example.radioplayer.ui.animations.SwipeToDeleteCallback
import com.example.radioplayer.ui.animations.objectSizeScaleAnimation
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.viewmodels.SWITCH_BOOKMARKS
import com.example.radioplayer.ui.viewmodels.SWITCH_STATIONS_ALL
import com.example.radioplayer.ui.viewmodels.SWITCH_STATIONS_ONE_DATE
import com.example.radioplayer.ui.viewmodels.SWITCH_TITLES_ALL
import com.example.radioplayer.ui.viewmodels.SWITCH_TITLES_ONE_DATE
import com.example.radioplayer.ui.viewmodels.TAB_BOOKMARKS
import com.example.radioplayer.ui.viewmodels.TAB_STATIONS
import com.example.radioplayer.ui.viewmodels.TAB_TITLES
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.FRAG_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY_ONE_DATE
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.example.radioplayer.utils.SpinnerExt
import com.example.radioplayer.utils.TextViewOutlined
import com.example.radioplayer.utils.Utils
import com.example.radioplayer.utils.Utils.fromDateToString
import com.example.radioplayer.utils.addAction
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.sql.Date
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(
    FragmentHistoryBinding::inflate
) {


    private lateinit var datesAdapter : HistoryDatesAdapter

    @Inject
    lateinit var glide : RequestManager

    private var isInitialLoad = true

    private var stationsHistoryAdapter: PagingHistoryAdapter? = null

    private var titlesHistoryAdapter : TitleAdapter? = null

    @Inject
    lateinit var bookmarkedTitlesAdapter : BookmarkedTitlesAdapter

    private var isStationsAdapterSet = false
    private var isTitlesAdapterSet = false

    private var isToHandleNewStationObserver = false

    private var isBookmarkedTitlesObserverSet = false

    private var rvState : Parcelable? = null


    private val clipBoard : ClipboardManager? by lazy {
        ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
    }


    companion object{

        var adapterAnimator = AdapterAnimator()

//       var isNewHistoryQuery = true
       var numberOfDates = 0

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeIsInBookmarks()

        observeIsInStationsTab()

        setupRecyclerView()

        observePlaybackState()

        observeNewStation()

        setupAdapterClickListener()

        observeListOfDates()

//        setTvSelectDateClickListener()


        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
            setSpinnerOpenCloseListener()
        }


        setTitlesClickListener()

        setStationsClickListener()

        switchTitlesStationsUi(false)

        setBookmarksFabClickListener()

        setFabPickDateClickListener()


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                historyViewModel.currentTab.collectLatest {
                    when(it){
                        TAB_STATIONS -> {
                            setStationsHistoryAdapter()
                            bind.fabBookmarkedTitles.isVisible = false
                            bind.fabDatePick.setImageResource(R.drawable.ic_history_pick_date)

                        }
                        TAB_TITLES -> {

                            bind.fabBookmarkedTitles.setImageResource(R.drawable.ic_bookmark_hollow)
                            bind.fabBookmarkedTitles.isVisible = true
                            bind.fabDatePick.setImageResource(R.drawable.ic_history_pick_date)
                            setTitlesHistoryAdapter()
                        }
                        TAB_BOOKMARKS -> {
                            switchToBookmarkedTitles()
                            bind.fabBookmarkedTitles.setImageResource(R.drawable.ic_bookmark_selected)
                            bind.fabBookmarkedTitles.isVisible = true
                            bind.fabDatePick.setImageResource(R.drawable.ic_history_pick_date_inactive)

                        }
                    }
                }
            }
        }
    }


    private fun setFabPickDateClickListener(){
        bind.fabDatePick.setOnClickListener {

            if(!historyViewModel.isInStationsTab && historyViewModel.isInBookmarks){
                /*DO NOTHING*/
            } else {
                bind.spinnerDates.performClick()
            }
        }
    }


    private fun observeIsInStationsTab(){

        historyViewModel.isHistoryInStationsTabLiveData.observe(viewLifecycleOwner){

            historyViewModel.isInStationsTab = it

//            bind.fabBookmarkedTitles.isVisible = !it

//            switchButtonsVisibilityWithBookmarks()

        }

    }


    private fun observeIsInBookmarks(){

        historyViewModel.isInBookmarksLiveData.observe(viewLifecycleOwner){
            historyViewModel.isInBookmarks = it

//            bind.fabBookmarkedTitles.apply {
//                if(it)
//                    setImageResource(R.drawable.ic_bookmark_selected)
//                else
//                    setImageResource(R.drawable.ic_bookmark_hollow)
//            }

//            switchButtonsVisibilityWithBookmarks()

        }

    }


    private fun switchButtonsVisibilityWithBookmarks(){

        if(!historyViewModel.isInStationsTab && historyViewModel.isInBookmarks){
            bind.fabDatePick.setImageResource(R.drawable.ic_history_pick_date_inactive)
//            bind.spinnerDates.isVisible = false

        } else {
            bind.fabDatePick.setImageResource(R.drawable.ic_history_pick_date)
//            bind.spinnerDates.isVisible = true
        }
    }


    private fun setSpinnerOpenCloseListener(){

        bind.spinnerDates.setSpinnerEventsListener( object : SpinnerExt.OnSpinnerEventsListener{
            override fun onSpinnerOpened(spinner: Spinner?) {

//                bind.tvShrinkArrow.
//                    setCompoundDrawablesWithIntrinsicBounds(0, 0,
//                        R.drawable.ic_playlists_arrow_shrink,0)

                bind.separatorDefault?.visibility = View.INVISIBLE
                bind.separatorDefault?.slideAnim(200, 100, R.anim.fade_out_anim)

            }

            override fun onSpinnerClosed(spinner: Spinner?) {
//                bind.tvShrinkArrow.
//                setCompoundDrawablesWithIntrinsicBounds(0, 0,
//                    R.drawable.ic_playlists_arrow_expand,0)

                bind.separatorDefault?.visibility = View.VISIBLE

                bind.separatorDefault?.slideAnim(300, 0, R.anim.fade_in_anim)

            }
        })
    }






    private fun setupDatesSpinner(list : List<HistoryDate>){

        datesAdapter = HistoryDatesAdapter(list, requireContext())
        bind.spinnerDates.adapter = datesAdapter
        datesAdapter.selectedColor = ContextCompat.getColor(requireContext(), R.color.color_non_interactive)

    }




    private fun observeListOfDates(){

        historyViewModel.listOfDates.observe(viewLifecycleOwner){

            numberOfDates = it.size

            val allHistory = HistoryDate("All dates", 0)

            val dates = it.toMutableList()
            dates.add(0, allHistory)

            setupDatesSpinner(dates)

            var pos = dates.indexOfFirst { historyDate ->
                historyDate.time == historyViewModel.selectedDate
            }


            datesAdapter.selectedItemPosition = pos

//            setSliderHeaderText(databaseViewModel.selectedDate)

            bind.spinnerDates.setSelection(pos)

            setDatesSpinnerSelectListener()

            subscribeToHistory()

            Log.d("CHECKTAGS", "check if with adding new date this code is running twice")

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

                if(historyViewModel.selectedDate != item.time){

                    datesAdapter.selectedItemPosition = position

//                    setSliderHeaderText(item.time)

                    historyViewModel.updateSelectedDate(item.time)

//                    loadHistory()
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


//    private fun loadHistory(){
//
//        isInitialLoad = true
//        adapterAnimator.resetAnimator()

//        rvState?.let {
//            bind.rvHistory.layoutManager?.onRestoreInstanceState(it)
//        }

//        historyViewModel.apply {

//            if(!isInStationsTab && isSameTab){
//                titlesHistoryAdapter?.submitData(lifecycle, PagingData.empty())
//            }

//            if(selectedDate == 0L){
//                if(isInStationsTab){
//                    getHistory(SWITCH_STATIONS_ALL)
//                } else {
//                    getHistory(SWITCH_TITLES_ALL)
//                }
//
//            } else {
//                if(isInStationsTab){
//                    getHistory(SWITCH_STATIONS_ONE_DATE)
//                } else {
//                    isTitleOneDateHeaderSet = false
//                    getHistory(SWITCH_TITLES_ONE_DATE)
//                }
//            }
//        }
//    }

//    private fun setSliderHeaderText(time : Long){
//
//            if(time == 0L) (bind.tvSliderHeader as TextView).text = "All dates"
//
//            else{
//                calendar.time = Date(time)
//
//                val date = Utils.fromDateToStringShort(calendar)
//
//                (bind.tvSliderHeader as TextView).text = date
//            }
//
//    }


    private fun observePlaybackState(){
        mainViewModel.playbackState.observe(viewLifecycleOwner){
            it?.let {

                when{
                    it.isPlaying -> {
                        stationsHistoryAdapter?.utils?.currentPlaybackState = true

                            stationsHistoryAdapter?.updateStationPlaybackState()

                    }
                    it.isPlayEnabled -> {
                        stationsHistoryAdapter?.utils?.currentPlaybackState = false

                            stationsHistoryAdapter?.updateStationPlaybackState()

                    }
                }
            }
        }
    }


    private fun observeNewStation(){

        RadioService.currentPlayingStation.observe(viewLifecycleOwner){ station ->

            if(historyViewModel.isInStationsTab &&
                    RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS){

                if(isToHandleNewStationObserver){

                    val index =

                        if(historyViewModel.selectedDate == RadioService.selectedHistoryDate){
                            RadioService.currentPlayingItemPosition + 1
                        } else if(
                            historyViewModel.selectedDate == 0L
                            && RadioService.currentMediaItems == SEARCH_FROM_HISTORY) {
                            adjustHistoryIndex()
                        } else {
                            stationsHistoryAdapter?.snapshot()?.items?.indexOfFirst {
                                it is StationWithDateModel.Station && it.radioStation.stationuuid == station.stationuuid
                            }
                        }

                    if(index != -1 && index != null){
                            handleNewRadioStation(index, station)
                        } else {
                            stationsHistoryAdapter?.updateOnStationChange(station, null)
                    }

                } else {
                    isToHandleNewStationObserver = true
                }
            }
        }
    }


    private fun adjustHistoryIndex() : Int {

        val map = mainViewModel.radioSource.allHistoryMap
        val index = RadioService.currentPlayingItemPosition

        if(index < map[0] - 2)
            return index + 1

        else {
            var shift = 3

            for(i in 1 until map.size){

                if (index < map[i] - 2)
                    break
                else shift += 2
            }

            return index + shift

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
            rvState = LinearLayoutManager(requireContext()).onSaveInstanceState()

            edgeEffectFactory = BounceEdgeEffectFactory()
//            itemAnimator = null
//            layoutAnimation = (activity as MainActivity).layoutAnimationController

//            setRvLoadChildrenListener()

            addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if(newState == RecyclerView.SCROLL_STATE_DRAGGING)
                        adapterAnimator.cancelAnimator()
                }
            })

//            if(historyViewModel.isInStationsTab){
//                setStationsHistoryAdapter()
//            } else {
//
//                if(historyViewModel.isInBookmarks){
//                    switchToBookmarkedTitles()
//                    bind.fabBookmarkedTitles.setImageResource(R.drawable.ic_bookmark_selected)
//                } else {
//                    setTitlesHistoryAdapter()
//                }
//            }
        }
    }


    private fun setStationsHistoryAdapter(){

        if(!isStationsAdapterSet){

            stationsHistoryAdapter = PagingHistoryAdapter(glide)

            stationsHistoryAdapter?.apply {

                setAdapterValues(utils)

                addOnPagesUpdatedListener {
                    if(isInitialLoad){
                        isInitialLoad = false
                        bind.rvHistory.apply {
                            scrollToPosition(0)
                        }
                    }
                }


                if(RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS){
                    RadioService.currentPlayingStation.value?.let {
                        val id =  it.stationuuid
                        currentRadioStationID = id
                    }
                } else {
                    currentRadioStationID = ""
                }
            }

            historyViewModel.setHistoryLiveData()

            isStationsAdapterSet = true
        }


        bind.rvHistory.apply {
            adapter = stationsHistoryAdapter
            setHasFixedSize(true)
        }

        itemTouchHelper.attachToRecyclerView(null)
    }

    private fun setTitlesHistoryAdapter(){

        if(!isTitlesAdapterSet){
            titlesHistoryAdapter = TitleAdapter(glide)
//            setTitlesAdapterLoadStateListener()
            titlesHistoryAdapter?.apply {
                alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
                titleSize = settingsViewModel.stationsTitleSize
                setOnClickListener { title ->
                  handleTitleClick(title.title)
                }

                onBookmarkClickListener { title ->

                    Toast.makeText(requireContext(), "Title bookmarked", Toast.LENGTH_SHORT).show()

                    historyViewModel.upsertBookmarkedTitle(title)

                }

                addOnPagesUpdatedListener {
                    Log.d("CHECKTAGS", "scrolling up every time pages updated?")

                    if(isInitialLoad){
                        isInitialLoad = false
                        bind.rvHistory.apply {
                            scrollToPosition(0)
                        }
                    }

                }

            }
            historyViewModel.setTitlesLiveData()
            isTitlesAdapterSet = true
        }

        bind.rvHistory.apply {
            adapter = titlesHistoryAdapter
            setHasFixedSize(false)
        }

        itemTouchHelper.attachToRecyclerView(null)
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

            if(historyViewModel.isInStationsTab){

                historyViewModel.isInStationsTab = false
                historyViewModel.isHistoryInStationsTabLiveData.postValue(false)
                historyViewModel.setIsInStations(false)


//                if(historyViewModel.isInBookmarks){
//                    switchToBookmarkedTitles()
//
//                } else {
//                    handleSwitchToNotMarkedTitles()
//                }

            }

            switchTitlesStationsUi(true)

        }
    }




    private fun handleSwitchToNotMarkedTitles(){

        setTitlesHistoryAdapter()

//        if(databaseViewModel.selectedDate != 0L){
//            titlesHistoryAdapter?.submitData(lifecycle, PagingData.empty())
//        }

//        loadHistory()
    }

    private fun setBookmarksFabClickListener(){

        bind.fabBookmarkedTitles.apply {

            setOnClickListener {

                historyViewModel.isInBookmarks = !historyViewModel.isInBookmarks

                historyViewModel.isInBookmarksLiveData.postValue(historyViewModel.isInBookmarks)
                historyViewModel.setIsInBookmarks(historyViewModel.isInBookmarks)

//                adapterAnimator.resetAnimator()

//                if(historyViewModel.isInBookmarks){
//                    historyViewModel.getHistory(SWITCH_BOOKMARKS)
//                    switchToBookmarkedTitles()

//                }
//                else {
//
//                    handleSwitchToNotMarkedTitles()
//                }
            }
        }
    }



    private fun switchToBookmarkedTitles(){

        bind.rvHistory.apply {
            adapter = bookmarkedTitlesAdapter
            setHasFixedSize(false)
        }


        itemTouchHelper.attachToRecyclerView(bind.rvHistory)


        if(!isBookmarkedTitlesObserverSet){

            isBookmarkedTitlesObserverSet = true

            bookmarkedTitlesAdapter.apply {
                alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
                titleSize = settingsViewModel.stationsTitleSize

                setOnClickListener { title ->
                    handleTitleClick(title.title)
                }
            }

//            historyViewModel.bookmarkedTitlesLivedata.observe(viewLifecycleOwner){
//
//                bookmarkedTitlesAdapter.listOfTitles = it
//
//            }
        }

//        else {
//            bind.rvHistory.scheduleLayoutAnimation()
//        }
    }





    private fun setStationsClickListener(){

        bind.tvStations.setOnClickListener {

            if(!historyViewModel.isInStationsTab){

                historyViewModel.isInStationsTab = true
                historyViewModel.isHistoryInStationsTabLiveData.postValue(true)
                historyViewModel.setIsInStations(true)

                setStationsHistoryAdapter()

//                if(databaseViewModel.selectedDate != 0L){
//                    stationsHistoryAdapter?.submitData(lifecycle, PagingData.empty())
//                }

                switchTitlesStationsUi(true)

//                loadHistory()
            }
        }
    }



    private fun switchTitlesStationsUi(isToAnimate: Boolean) =
        SwapTitlesUi.swap(
            conditionA = historyViewModel.isInStationsTab,
            textViewA = bind.tvStations as TextView,
            textViewB = bind.tvTitles as TextView,
            isToAnimate = isToAnimate,
            toolbar = bind.viewToolbar,
            fragment = FRAG_HISTORY
        )

    private fun subscribeToHistory(){

        observeHistory()



//        historyViewModel.observableHistory.observe(viewLifecycleOwner){
//
//                stationsHistoryAdapter?.submitData(lifecycle, it)
//
//            }
//
//        historyViewModel.observableTitles.observe(viewLifecycleOwner){
//
//            titlesHistoryAdapter?.submitData(lifecycle, it)
//
//        }
    }


    private fun observeHistory(){

        historyViewModel.initiateHistory()

        historyViewModel.observableHistoryPages?.observe(viewLifecycleOwner){

            isInitialLoad = true
            adapterAnimator.resetAnimator()

            when(historyViewModel.historySwitchValue){
                in SWITCH_STATIONS_ALL..SWITCH_STATIONS_ONE_DATE -> stationsHistoryAdapter
                    ?.submitData(lifecycle, it as PagingData<StationWithDateModel>)
                in SWITCH_TITLES_ALL..SWITCH_TITLES_ONE_DATE -> titlesHistoryAdapter
                    ?.submitData(lifecycle, it as PagingData<TitleWithDateModel>)
                SWITCH_BOOKMARKS -> bookmarkedTitlesAdapter.listOfTitles = it as List<BookmarkedTitle>
            }
        }
    }


    private fun setupAdapterClickListener(){

        stationsHistoryAdapter?.utils?.setOnClickListener { station, position ->

            val flag = if(historyViewModel.selectedDate == 0L){
                SEARCH_FROM_HISTORY
            } else
                SEARCH_FROM_HISTORY_ONE_DATE

          var isToChangeMediaItems = false

            if(historyViewModel.selectedDate > 0 &&
                RadioService.selectedHistoryDate != historyViewModel.selectedDate){
                RadioService.selectedHistoryDate = historyViewModel.selectedDate
                RadioSource.updateHistoryOneDateStations()
                isToChangeMediaItems = true
            }

            else if (RadioService.currentMediaItems != flag){
                isToChangeMediaItems = true
            }

           val isNewItem = mainViewModel.playOrToggleStation(station, flag,
                itemIndex = position, isToChangeMediaItems = isToChangeMediaItems)

            if(isToChangeMediaItems || isNewItem){
                isToHandleNewStationObserver = false
            }
        }
    }



    private val itemTouchCallback by lazy {

        object : SwipeToDeleteCallback(requireContext()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.layoutPosition

                val bookmark = bookmarkedTitlesAdapter.listOfTitles[position]

                historyViewModel.deleteBookmarkTitle(bookmark)

                Snackbar.make(
                    requireActivity().findViewById(R.id.rootLayout),
                    "Bookmark deleted",
                    Snackbar.LENGTH_LONG
                ).apply {

                    setAction("UNDO"){
                        historyViewModel.restoreBookmarkTitle(bookmark)
                    }
                }.show()
            }
        }
    }


    private val itemTouchHelper by lazy {
        ItemTouchHelper(itemTouchCallback)
    }




    override fun onDestroyView() {
        super.onDestroyView()

        historyViewModel.cleanHistoryTab()
//        isNewHistoryQuery = true
        isInitialLoad = true
        isStationsAdapterSet = false
        isTitlesAdapterSet = false
        bind.rvHistory.adapter = null
        stationsHistoryAdapter = null
        titlesHistoryAdapter = null
        _bind = null
        isToHandleNewStationObserver = false
        isBookmarkedTitlesObserverSet = false
        adapterAnimator.resetAnimator()
    }

}