package com.example.radioplayer.ui.fragments

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.*
import com.example.radioplayer.adapters.models.StationWithDateModel
import com.example.radioplayer.data.local.entities.HistoryDate
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.FragmentHistoryBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.example.radioplayer.ui.animations.SwapTitlesUi
import com.example.radioplayer.ui.animations.SwipeToDeleteCallback
import com.example.radioplayer.ui.animations.objectSizeScaleAnimation
import com.example.radioplayer.ui.animations.slideAnim
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

        setToolbar()

        setTitlesClickListener()

        setStationsClickListener()

        switchTitlesStationsUi(false)

        setBookmarksFabClickListener()

        setFabPickDateClickListener()

    }


    private fun setFabPickDateClickListener(){
        bind.fabDatePick.setOnClickListener {

            if(!databaseViewModel.isInStationsTab && databaseViewModel.isInBookmarks){
                /*DO NOTHING*/
            } else {
                bind.spinnerDates.performClick()
            }
        }
    }


    private fun observeIsInStationsTab(){

        databaseViewModel.isHistoryInStationsTabLiveData.observe(viewLifecycleOwner){

            databaseViewModel.isInStationsTab = it

            bind.fabBookmarkedTitles.isVisible = !it

            switchButtonsVisibilityWithBookmarks()

        }

    }


    private fun observeIsInBookmarks(){

        databaseViewModel.isInBookmarksLiveData.observe(viewLifecycleOwner){
            databaseViewModel.isInBookmarks = it

            bind.fabBookmarkedTitles.apply {
                if(it)
                    setImageResource(R.drawable.ic_bookmark_selected)
                else
                    setImageResource(R.drawable.ic_bookmark_hollow)
            }

            switchButtonsVisibilityWithBookmarks()

        }

    }


    private fun switchButtonsVisibilityWithBookmarks(){

        if(!databaseViewModel.isInStationsTab && databaseViewModel.isInBookmarks){
            bind.fabDatePick.setImageResource(R.drawable.ic_history_pick_date_inactive)
//            bind.tvSliderHeader.isVisible = false
            bind.spinnerDates.isVisible = false
//            bind.tvShrinkArrow.isVisible = false
//            bind.viewSpinnerClickBox?.isVisible = false
        } else {
            bind.fabDatePick.setImageResource(R.drawable.ic_history_pick_date)
//            bind.tvSliderHeader.isVisible = true
            bind.spinnerDates.isVisible = true
//            bind.tvShrinkArrow.isVisible = true
//            bind.viewSpinnerClickBox?.isVisible = true
        }
    }


    private fun setToolbar(){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            val color = ContextCompat.getColor(requireContext(), R.color.nav_bar_history_frag)
//            val colorStatus = ContextCompat.getColor(requireContext(), R.color.status_bar_history_frag)

            (activity as MainActivity).apply {
                window.navigationBarColor = color
                window.statusBarColor = color
            }



//            bind.viewSpinnerClickBox?.setOnTouchListener { v, event ->
//
//                if (event.action == MotionEvent.ACTION_DOWN){
//                    bind.tvSliderHeader.isPressed = true
//                bind.spinnerDates.performClick()
//                }
//
//                true
//            }

//        } else {
//            bind.viewToolbar.setBackgroundColor(Color.BLACK)
//            bind.tvSliderHeader.setOnClickListener {
//                bind.spinnerDates.performClick()
//            }
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

        databaseViewModel.listOfDates.observe(viewLifecycleOwner){

            numberOfDates = it.size

            val allHistory = HistoryDate("All dates", 0)

            val dates = it.toMutableList()
            dates.add(0, allHistory)

            setupDatesSpinner(dates)

            var pos = dates.indexOfFirst { historyDate ->
                historyDate.time == databaseViewModel.selectedDate
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

                if(databaseViewModel.selectedDate != item.time){

                    datesAdapter.selectedItemPosition = position

//                    setSliderHeaderText(item.time)

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

            if(!isInStationsTab && isSameTab){
                titlesHistoryAdapter?.submitData(lifecycle, PagingData.empty())
            }

            if(selectedDate == 0L){
                if(isInStationsTab){
                    getAllHistory()
                } else {
                    getAllTitles()
                }

            } else {
                if(isInStationsTab){
                    oneHistoryDateCaller.postValue(true)
                } else {
                    databaseViewModel.isTitleOneDateHeaderSet = false
                    oneTitleDateCaller.postValue(true)
                }
            }
        }

        isInitialLoad = true
    }

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

            if(databaseViewModel.isInStationsTab &&
                    RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS){

                if(isToHandleNewStationObserver){

                    val index =

                        if(databaseViewModel.selectedDate == RadioService.selectedHistoryDate){
                            RadioService.currentPlayingItemPosition + 1
                        } else if(
                            databaseViewModel.selectedDate == 0L
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
            edgeEffectFactory = BounceEdgeEffectFactory()
            itemAnimator = null
            layoutAnimation = (activity as MainActivity).layoutAnimationController

            setRvLoadChildrenListener()

            if(databaseViewModel.isInStationsTab){
                setStationsHistoryAdapter()
            } else {

                if(databaseViewModel.isInBookmarks){
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

                setAdapterValues(utils)

                if(RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS){
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

        itemTouchHelper.attachToRecyclerView(null)
    }

    private fun setTitlesHistoryAdapter(){

        if(!isTitlesAdapterSet){
            titlesHistoryAdapter = TitleAdapter(glide)
            setTitlesAdapterLoadStateListener()
            titlesHistoryAdapter?.apply {
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
            if(databaseViewModel.isInStationsTab){

                databaseViewModel.isInStationsTab = false
                databaseViewModel.isHistoryInStationsTabLiveData.postValue(false)


                if(databaseViewModel.isInBookmarks){
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

                databaseViewModel.isInBookmarks = !databaseViewModel.isInBookmarks

                databaseViewModel.isInBookmarksLiveData.postValue(databaseViewModel.isInBookmarks)


                if(databaseViewModel.isInBookmarks){

                    switchToBookmarkedTitles()

                } else {

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

        itemTouchHelper.attachToRecyclerView(bind.rvHistory)


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

            databaseViewModel.bookmarkedTitlesLivedata.value?.let {
                bind.rvHistory.scheduleLayoutAnimation()
            } ?: kotlin.run {
                bind.rvHistory.post {
                    bind.rvHistory.scheduleLayoutAnimation()
                }
            }


        } else {
            bind.rvHistory.scheduleLayoutAnimation()
        }
    }





    private fun setStationsClickListener(){

        bind.tvStations.setOnClickListener {

            if(!databaseViewModel.isInStationsTab){

                databaseViewModel.isInStationsTab = true
                databaseViewModel.isHistoryInStationsTabLiveData.postValue(true)

                setStationsHistoryAdapter()

                if(databaseViewModel.selectedDate != 0L){
                    stationsHistoryAdapter?.submitData(lifecycle, PagingData.empty())
                }

                switchTitlesStationsUi(true)

                loadHistory()
            }
        }
    }



    private fun switchTitlesStationsUi(isToAnimate: Boolean) =
        SwapTitlesUi.swap(
            conditionA = databaseViewModel.isInStationsTab,
            textViewA = bind.tvStations as TextView,
            textViewB = bind.tvTitles as TextView,
            isToAnimate = isToAnimate,
            toolbar = bind.viewToolbar,
            fragment = FRAG_HISTORY
        )

    private fun subscribeToHistory(){

            databaseViewModel.observableHistory.observe(viewLifecycleOwner){

                stationsHistoryAdapter?.submitData(lifecycle, it)
            }

        databaseViewModel.observableTitles.observe(viewLifecycleOwner){

            titlesHistoryAdapter?.submitData(lifecycle, it)

        }
    }

    private fun setupAdapterClickListener(){

        stationsHistoryAdapter?.utils?.setOnClickListener { station, position ->

            val flag = if(databaseViewModel.selectedDate == 0L){
                SEARCH_FROM_HISTORY
            } else
                SEARCH_FROM_HISTORY_ONE_DATE

          var isToChangeMediaItems = false

            if(databaseViewModel.selectedDate > 0 &&
                RadioService.selectedHistoryDate != databaseViewModel.selectedDate){
                RadioService.selectedHistoryDate = databaseViewModel.selectedDate
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

                databaseViewModel.deleteBookmarkTitle(bookmark)

                Snackbar.make(
                    requireActivity().findViewById(R.id.rootLayout),
                    "Bookmark deleted",
                    Snackbar.LENGTH_LONG
                ).apply {

                    setAction("UNDO"){
                        databaseViewModel.restoreBookmarkTitle(bookmark)
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

        databaseViewModel.cleanHistoryTab()
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