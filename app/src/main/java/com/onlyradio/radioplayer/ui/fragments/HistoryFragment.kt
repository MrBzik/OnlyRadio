package com.onlyradio.radioplayer.ui.fragments

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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.adapters.BookmarkedTitlesAdapter
import com.onlyradio.radioplayer.adapters.HistoryDatesAdapter
import com.onlyradio.radioplayer.adapters.PagingHistoryAdapter
import com.onlyradio.radioplayer.adapters.TitleAdapter
import com.onlyradio.radioplayer.adapters.models.StationWithDateModel
import com.onlyradio.radioplayer.data.local.entities.HistoryDate
import com.onlyradio.radioplayer.databinding.FragmentHistoryBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.onlyradio.radioplayer.ui.animations.SwapTitlesUi
import com.onlyradio.radioplayer.ui.animations.SwipeToDeleteCallback
import com.onlyradio.radioplayer.ui.animations.slideAnim
import com.onlyradio.radioplayer.ui.viewmodels.TAB_BOOKMARKS
import com.onlyradio.radioplayer.ui.viewmodels.TAB_STATIONS
import com.onlyradio.radioplayer.ui.viewmodels.TAB_TITLES
import com.onlyradio.radioplayer.utils.Constants.FRAG_HISTORY
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.onlyradio.radioplayer.utils.SpinnerExt
import com.onlyradio.radioplayer.utils.addAction
import com.google.android.material.snackbar.Snackbar
import com.onlyradio.radioplayer.domain.HistoryData
import com.onlyradio.radioplayer.domain.PlayingStationState
import com.onlyradio.radioplayer.extensions.makeToast
import com.onlyradio.radioplayer.extensions.observeFlow
import com.onlyradio.radioplayer.extensions.snackbarWithAction
import com.onlyradio.radioplayer.utils.Constants.CLICK_DEBOUNCE
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_HISTORY_ONE_DATE
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.onlyradio.radioplayer.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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

    private var isBookmarkedTitlesObserverSet = false

    private var rvState : Parcelable? = null

    private var currentTab = TAB_STATIONS

    private val clipBoard : ClipboardManager? by lazy {
        ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
    }

    private var stationsClickJob : Job? = null


    companion object{

       var isNewHistoryQuery = true
       var numberOfDates = 0

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        observeStationWithPlayback()

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

        var isSpinnerSet = false

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



    private fun observeStationWithPlayback(){

        observeFlow(mainViewModel.isPlayingFlow.combine(RadioService.currentPlayingStation.asFlow()){ isPlaying, station ->
            PlayingStationState(station?.stationuuid ?: "", isPlaying)
        }){ state ->

            if(historyViewModel.currentTab.value == TAB_STATIONS && RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS
            ){
                stationsHistoryAdapter?.setPlaybackState(state.isPlaying)

                val id = state.stationId

                val index = calculateIndex(id)

                if(index >= 0){
                    handleNewRadioStation(index, id)
                }
            }
        }
    }

    private fun calculateIndex(id : String) : Int {

        return if(
            RadioService.currentMediaItems == SEARCH_FROM_HISTORY_ONE_DATE &&
            historyViewModel.selectedDate == RadioService.selectedHistoryDate){
            mainViewModel.getPlayerCurrentIndex() + 1
        } else if(
            historyViewModel.selectedDate == 0L
            && RadioService.currentMediaItems == SEARCH_FROM_HISTORY) {
            adjustHistoryIndex()
        } else {
            stationsHistoryAdapter?.snapshot()?.items?.indexOfFirst {
                it is StationWithDateModel.Station && it.radioStation.stationuuid == id
            }
        } ?: -1


    }


    private fun adjustHistoryIndex() : Int {

        val map = mainViewModel.radioSource.allHistoryMap
        val index = mainViewModel.getPlayerCurrentIndex()

        var shift = 1

        for(i in map.indices){
            if(index < map[i] - shift - 1)
                break
            else shift += 2
        }

        return index + shift

    }


    private fun handleNewRadioStation(position : Int, stationId : String){

        if(stationsHistoryAdapter?.isSameId(stationId) == false)
            bind.rvHistory.smoothScrollToPosition(position)

        bind.rvHistory.post {
            bind.rvHistory.findViewHolderForAdapterPosition(position)?.let { holder ->
                if(holder is PagingHistoryAdapter.StationViewHolder)
                    stationsHistoryAdapter?.onNewPlayingItem(position, stationId, holder)
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

            stationsHistoryAdapter = PagingHistoryAdapter(
                glide,
            resources.getString(R.string.date_today)
                )

            stationsHistoryAdapter?.apply {

                initialiseValues(requireContext(), settingsViewModel.stationsTitleSize)

                addOnPagesUpdatedListener {
                    handleRvAnim()
                    RadioService.currentPlayingStation.value?.let { station ->
                        val index = calculateIndex(station.stationuuid)
                        updateSelectedValues(index, station.stationuuid)
                        setPlaybackState(mainViewModel.isPlaying.value ?: false)
                    }
                }

                onBindToNewPos { oldPos, newPos ->
                    bind.rvHistory.post {
                        try {
                            restoreState(oldPos)
                            historyViewModel.onBindingToNewPosition(oldPos, newPos)
                        } catch (e : Exception){
                            Logger.log(e.stackTraceToString())
                        }
                    }
                }

                setStationsAdapterLoadStateListener()
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
            titlesHistoryAdapter = TitleAdapter(
                glide,
            requireContext().resources.getString(R.string.date_today)
                )
            titlesHistoryAdapter?.apply {
                alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
                titleSize = settingsViewModel.stationsTitleSize
                setOnClickListener { title ->
                  handleTitleClick(title.title)
                }

                onBookmarkClickListener { title ->

                    requireContext().makeToast(R.string.title_bookmarked)

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
            resources.getString(R.string.title_copied), Snackbar.LENGTH_LONG).apply {

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
                } catch (e : Exception){
                    val webIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=$title"))
                    try {
                        startActivity(webIntent)
                    } catch (e : Exception){
                        requireContext().makeToast(R.string.no_browser_error)
                    }

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
            fragment = FRAG_HISTORY,
        )

    private fun subscribeToHistory(){

        historyViewModel.initiateHistory()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){

                historyViewModel.observableHistoryPages?.collectLatest{ tab ->

                    when(tab){
                        is HistoryData.StationsFlow -> {
                            bind.tvHistoryMessage.visibility = View.INVISIBLE
                            stationsHistoryAdapter?.submitData(lifecycle, tab.data)

                        }

                        is HistoryData.TitlesFlow -> {
                            bind.tvHistoryMessage.visibility = View.INVISIBLE
                            titlesHistoryAdapter?.submitData(lifecycle, tab.data)
                        }

                        is HistoryData.Bookmarks -> {

                            bookmarkedTitlesAdapter.listOfTitles = tab.list
                            withContext(Dispatchers.Main){
                                bind.tvHistoryMessage.apply {
                                    if(tab.list.isEmpty()){
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

        stationsHistoryAdapter?.setOnClickListener { station, position, isToSwap ->

            if(stationsClickJob?.isActive == true)
                return@setOnClickListener

            stationsClickJob = lifecycleScope.launch {
                val params = historyViewModel.isToChangeMediaItems()

                mainViewModel.playOrToggleStation(
                    stationId = station.stationuuid,
                    searchFlag = params.first,
                    itemIndex = position,
                    isToChangeMediaItems = params.second,
                    isHistorySwap = isToSwap
                )
                delay(CLICK_DEBOUNCE)
            }



//            if(isToChangeMediaItems || isNewItem){
//                isToHandleNewStationObserver = false
//            }
        }
    }



    private val itemTouchCallback by lazy {

        object : SwipeToDeleteCallback(requireContext()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.bindingAdapterPosition

                val bookmark = bookmarkedTitlesAdapter.listOfTitles[position]

                historyViewModel.deleteBookmarkTitle(bookmark)

                requireActivity().snackbarWithAction(R.string.bookmark_deleted){
                    historyViewModel.restoreBookmarkTitle(bookmark)
                }
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
        isBookmarkedTitlesObserverSet = false
    }


}