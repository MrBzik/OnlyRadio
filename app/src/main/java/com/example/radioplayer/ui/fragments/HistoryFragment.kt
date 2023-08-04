package com.example.radioplayer.ui.fragments

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
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
import com.example.radioplayer.adapters.BookmarkedTitlesAdapter
import com.example.radioplayer.adapters.HistoryDatesAdapter
import com.example.radioplayer.adapters.PagingHistoryAdapter
import com.example.radioplayer.adapters.TitleAdapter
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
import com.example.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.example.radioplayer.ui.animations.SwapTitlesUi
import com.example.radioplayer.ui.animations.SwipeToDeleteCallback
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.viewmodels.TAB_BOOKMARKS
import com.example.radioplayer.ui.viewmodels.TAB_STATIONS
import com.example.radioplayer.ui.viewmodels.TAB_TITLES
import com.example.radioplayer.utils.Constants.FRAG_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY_ONE_DATE
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.example.radioplayer.utils.SpinnerExt
import com.example.radioplayer.utils.addAction
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var isToHandleNewStationObserver = false

    private var isBookmarkedTitlesObserverSet = false

    private var rvState : Parcelable? = null

    private var currentTab = TAB_STATIONS

    private val clipBoard : ClipboardManager? by lazy {
        ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
    }


    companion object{

       var isNewHistoryQuery = true
       var numberOfDates = 0

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        observePlaybackState()

        observeNewStation()

        observeListOfDates()

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_YES){
            setSpinnerOpenCloseListener()
        }

        setTitlesClickListener()

        setStationsClickListener()

        switchTitlesStationsUi(false)

        setBookmarksFabClickListener()

        setFabPickDateClickListener()

        collectCurrentTabChanges()

    }


    private fun collectCurrentTabChanges(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                historyViewModel.currentTab.collectLatest {
                    currentTab = it
                    when(it){
                        TAB_STATIONS -> {
                            setStationsHistoryAdapter()
                            bind.fabBookmarkedTitles.isVisible = false
                            bind.fabDatePick.setImageResource(R.drawable.ic_history_pick_date)
                        }
                        TAB_TITLES -> {
                            setTitlesHistoryAdapter()
                            bind.fabBookmarkedTitles.setImageResource(R.drawable.ic_bookmark_hollow)
                            bind.fabBookmarkedTitles.isVisible = true
                            bind.fabDatePick.setImageResource(R.drawable.ic_history_pick_date)
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


    private fun setStationsAdapterLoadStateListener(){

        stationsHistoryAdapter?.addLoadStateListener {

            if (it.refresh !is LoadState.Loading && it.append !is LoadState.Loading) {
                handleRvAnim()
            }
        }
    }

    private fun setTitlesAdapterLoadStateListener(){

        titlesHistoryAdapter?.addLoadStateListener {

            if (it.refresh !is LoadState.Loading && it.append !is LoadState.Loading) {
                handleRvAnim()
            }
        }
    }


    private fun setRvLoadChildrenListener(){
        bind.rvHistory.addOnChildAttachStateChangeListener(
            object : RecyclerView.OnChildAttachStateChangeListener{
                override fun onChildViewAttachedToWindow(view: View) {
                    handleRvAnim()
                }

                override fun onChildViewDetachedFromWindow(view: View) {

                }
            }
        )
    }

    private fun handleRvAnim(){
        if(isInitialLoad){

            isInitialLoad = false

            // attaching only now to negate old adapter's items
            if(currentTab == TAB_STATIONS)
                attachStationsAdapter()
            else if(currentTab == TAB_TITLES)
                attachTitlesAdapter()

            bind.rvHistory.apply {
                scrollToPosition(0)
                startLayoutAnimation()
            }
        }
    }

    private fun setFabPickDateClickListener(){
        bind.fabDatePick.setOnClickListener {
            if(currentTab != TAB_BOOKMARKS){
                bind.spinnerDates.performClick()
            }
        }
    }



    private fun setSpinnerOpenCloseListener(){

        bind.spinnerDates.setSpinnerEventsListener( object : SpinnerExt.OnSpinnerEventsListener{
            override fun onSpinnerOpened(spinner: Spinner?) {

                bind.separatorDefault?.visibility = View.INVISIBLE
                bind.separatorDefault?.slideAnim(200, 100, R.anim.fade_out_anim)

            }

            override fun onSpinnerClosed(spinner: Spinner?) {

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

            if(!isSpinnerSet){
                isSpinnerSet = true

                setupDatesSpinner(dates)
                val pos = dates.indexOfFirst { historyDate ->
                    historyDate.time == historyViewModel.selectedDate
                }

                datesAdapter.selectedItemPosition = pos

                bind.spinnerDates.setSelection(pos)

                setDatesSpinnerSelectListener()

                // subscribing now due to datasource needs number of dates
                subscribeToHistory()

            } else {

                datesAdapter.apply {
                    datesList = dates
                    if(historyViewModel.selectedDate > 0L)
                    selectedItemPosition += 1
                }
            }
        }
    }


    private var isSpinnerSet = false




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

                    isInitialLoad = true

                    historyViewModel.updateSelectedDate(item.time)

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }




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

//            Log.d("CHECKTAGS", "collecting currentplayingstation again?")

            if(historyViewModel.currentTab.value == TAB_STATIONS &&
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
            itemAnimator = null
            layoutAnimation = (activity as MainActivity).layoutAnimationController

            setRvLoadChildrenListener()

        }
    }


    private fun setStationsHistoryAdapter(){

        if(stationsHistoryAdapter == null){

            stationsHistoryAdapter = PagingHistoryAdapter(glide)

            stationsHistoryAdapter?.apply {

                utils.initialiseValues(requireContext(), settingsViewModel.stationsTitleSize)

                addOnPagesUpdatedListener {
                    handleRvAnim()
                }

                setStationsAdapterLoadStateListener()

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

            setupAdapterClickListener()

        }
    }
    private fun attachStationsAdapter(){
        bind.rvHistory.apply {
            if(adapter !is PagingHistoryAdapter)
                adapter = stationsHistoryAdapter
            setHasFixedSize(true)
            itemTouchHelper.attachToRecyclerView(null)
        }
    }

    private fun setTitlesHistoryAdapter(){

        if(titlesHistoryAdapter == null){
            titlesHistoryAdapter = TitleAdapter(glide)
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
                    handleRvAnim()
                }

                setTitlesAdapterLoadStateListener()

            }
            historyViewModel.setTitlesLiveData()
        }
    }

    private fun attachTitlesAdapter(){
        bind.rvHistory.apply {
            if(adapter !is TitleAdapter)
                adapter = titlesHistoryAdapter
            setHasFixedSize(false)
            itemTouchHelper.attachToRecyclerView(null)
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
                try{
                    startActivity(intent)
                }catch (e : Exception){
                    val webIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=$title"))
                    startActivity(webIntent)
                }
            }
        }.show()
    }



    private fun setBookmarksFabClickListener(){

        bind.fabBookmarkedTitles.apply {

            setOnClickListener {

                isInitialLoad = true

                historyViewModel.setIsInBookmarks()

            }
        }
    }



    private fun switchToBookmarkedTitles(){

        bind.rvHistory.apply {
            if(adapter !is BookmarkedTitlesAdapter)
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
        }
    }



    private fun setTitlesClickListener(){

        bind.tvTitles.setOnClickListener {

            if(currentTab == TAB_STATIONS){

                isInitialLoad = true

                historyViewModel.setIsInStations(false)

                switchTitlesStationsUi(true)
            }
        }
    }



    private fun setStationsClickListener(){

        bind.tvStations.setOnClickListener {

            if(currentTab != TAB_STATIONS){

                isInitialLoad = true

                historyViewModel.setIsInStations(true)

                switchTitlesStationsUi(true)

            }
        }
    }



    private fun switchTitlesStationsUi(isToAnimate: Boolean) =
        SwapTitlesUi.swap(
            conditionA = historyViewModel.currentTab.value == TAB_STATIONS,
            textViewA = bind.tvStations as TextView,
            textViewB = bind.tvTitles as TextView,
            isToAnimate = isToAnimate,
            toolbar = bind.viewToolbar,
            fragment = FRAG_HISTORY
        )

    private fun subscribeToHistory(){

        historyViewModel.initiateHistory()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){

                historyViewModel.observableHistoryPages?.collectLatest{
                    when(historyViewModel.currentTab.value){
                        TAB_STATIONS -> {
                            stationsHistoryAdapter
                                ?.submitData(lifecycle, it as PagingData<StationWithDateModel>)
                            bind.tvHistoryMessage.visibility = View.INVISIBLE
                        }
                        TAB_TITLES -> {
                            titlesHistoryAdapter
                                ?.submitData(lifecycle, it as PagingData<TitleWithDateModel>)
                            bind.tvHistoryMessage.visibility = View.INVISIBLE
                        }
                        TAB_BOOKMARKS -> {
                            bookmarkedTitlesAdapter.listOfTitles = it as List<BookmarkedTitle>
                            withContext(Dispatchers.Main){
                                bind.tvHistoryMessage.apply {
                                    if(it.isEmpty()){
                                        text = requireContext().resources.getString(R.string.bookmarks_message)
                                        visibility = View.VISIBLE
                                        slideAnim(400, 0, R.anim.fade_in_anim)
                                    } else {
                                        visibility = View.INVISIBLE
                                    }
                                }
                            }
                        }
                    }
                }
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

                val position = viewHolder.absoluteAdapterPosition

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


    private val itemTouchHelper by lazy(mode = LazyThreadSafetyMode.NONE) {
        ItemTouchHelper(itemTouchCallback)
    }




    override fun onDestroyView() {
        super.onDestroyView()

        historyViewModel.cleanHistoryTab()
        isNewHistoryQuery = true
        isInitialLoad = true
        bind.rvHistory.adapter = null
        stationsHistoryAdapter = null
        titlesHistoryAdapter = null
        _bind = null
        isToHandleNewStationObserver = false
        isBookmarkedTitlesObserverSet = false
        isSpinnerSet = false
    }


}