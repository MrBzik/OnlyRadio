package com.example.radioplayer.ui.fragments


import android.content.res.Configuration
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PlaylistsAdapter
import com.example.radioplayer.adapters.RadioDatabaseAdapter
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.databinding.FragmentFavStationsBinding
import com.example.radioplayer.exoPlayer.*
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.*
import com.example.radioplayer.ui.dialogs.CreatePlaylistDialog
import com.example.radioplayer.ui.dialogs.EditPlaylistDialog
import com.example.radioplayer.ui.dialogs.RemovePlaylistDialog
import com.example.radioplayer.ui.viewmodels.PixabayViewModel
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_LAZY_LIST
import com.example.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.example.radioplayer.utils.dpToP
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject



@AndroidEntryPoint
class FavStationsFragment : BaseFragment<FragmentFavStationsBinding>(
    FragmentFavStationsBinding::inflate
) {


    private var currentPlaylistName : String = ""
    private lateinit var listOfPlaylists : List<Playlist>
    lateinit var pixabayViewModel: PixabayViewModel

//    private var isInFavouriteTab = false

    private var currentTab = SEARCH_FROM_FAVOURITES

    private var isPlaylistsVisible = false

    private var searchFlag : Int = 1

    private var currentStation : RadioStation? = null

    @Inject
    lateinit var mainAdapter: RadioDatabaseAdapter

    val playlistAdapter : PlaylistsAdapter by lazy {
        PlaylistsAdapter(glide, true)
    }

    @Inject
    lateinit var glide : RequestManager

    private var currentPlaylistPosition = 0

    private var isToHandleNewStationObserver = false

    companion object{

        var dragAndDropItemPos = -1
        var dragAndDropStation : RadioStation? = null

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pixabayViewModel = ViewModelProvider(requireActivity())[PixabayViewModel::class.java]

        setupMainRecycleView()

        setupPlaylistRecycleView()

        observeNewStation()

        observePlaybackState()

        subscribeToObservers()

        setMainAdapterClickListener()

        setPlaylistAdapterClickListeners()

        setArrowToFavClickListener()

        setOnPlaylistsExpandClickListener()

        observePlaylistsVisibilityState()

        editPlaylistClickListener()



    }




    private fun observeNewStation(){

        RadioService.currentPlayingStation.observe(viewLifecycleOwner) { station ->

            currentStation = station

            if(RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS){

                if (isToHandleNewStationObserver) {

                    val index = getCurrentItemPosition(station)

                    if (index != -1) {
                        handleNewRadioStation(index, station)
                    } else {
                        mainAdapter.updateOnStationChange(station, null)
                    }

                } else{
                    isToHandleNewStationObserver = true
                }
            }
        }
    }

    private fun getCurrentItemPosition(station : RadioStation?) : Int{
        if(currentTab == SEARCH_FROM_FAVOURITES && RadioService.currentMediaItems == SEARCH_FROM_FAVOURITES ||
            currentTab == SEARCH_FROM_PLAYLIST && RadioService.currentMediaItems == SEARCH_FROM_PLAYLIST &&
            currentPlaylistName == RadioService.currentPlaylistName ||
            currentTab == SEARCH_FROM_LAZY_LIST && RadioService.currentMediaItems == SEARCH_FROM_LAZY_LIST
        )
            return RadioService.currentPlayingItemPosition

        else return mainAdapter.listOfStations
            .indexOfFirst {
                it.stationuuid == station?.stationuuid
            }
    }

    private fun handleNewRadioStation(position : Int, station : RadioStation){

        bind.rvFavStations.smoothScrollToPosition(position)

        bind.rvFavStations.post {

            val holder = bind.rvFavStations
                .findViewHolderForAdapterPosition(position)

            holder?.let {
                mainAdapter.updateOnStationChange(station, holder as RadioDatabaseAdapter.RadioItemHolder)
            }
        }
    }


    private fun observePlaybackState(){

        mainViewModel.playbackState.observe(viewLifecycleOwner){

            it?.let {

                when{
                    it.isPlaying -> {
                        mainAdapter.utils.currentPlaybackState = true
                        mainAdapter.updateStationPlaybackState()
                    }
                    it.isPlayEnabled -> {
                        mainAdapter.utils.currentPlaybackState = false
                        mainAdapter.updateStationPlaybackState()
                    }
                }
            }
        }
    }


    private fun editPlaylistClickListener(){

        bind.tvPlaylistEdit.setOnClickListener {

            if(currentTab == SEARCH_FROM_PLAYLIST){

                var isDeletePlaylistCalled = false

                val dialog =  EditPlaylistDialog (
                    requireContext(), listOfPlaylists,
                    currentPlaylistName,
                    currentPlaylistPosition,
                    databaseViewModel, pixabayViewModel, glide
                ) {
                    isDeletePlaylistCalled = it
                }

                dialog.show()

                dialog.setOnDismissListener {

                    if(isDeletePlaylistCalled){

                        RemovePlaylistDialog(requireContext(), currentPlaylistName){

                            databaseViewModel.deletePlaylistAndContent(currentPlaylistName)

                            databaseViewModel.getAllFavouredStations()

                        }.show()
                    }
                }
            }
        }
    }


    private fun observePlaylistsVisibilityState(){

        pixabayViewModel.togglePlaylistsVisibility.observe(viewLifecycleOwner){

            isPlaylistsVisible = it

            bind.rvPlaylists.isVisible = it
            
            bind.ivArrowBackToFav.isVisible = it && currentTab != SEARCH_FROM_FAVOURITES


            (bind.tvPlaylistsExpand as TextView).apply {
                    if(it){
                        setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_playlists_arrow_shrink, 0)
                    } else {
                        setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_playlists_arrow_expand, 0)
                    }
                }
        }
    }




    private fun setOnPlaylistsExpandClickListener(){
        bind.tvPlaylistsExpand.setOnClickListener {
           pixabayViewModel.togglePlaylistsVisibility.postValue(
               !isPlaylistsVisible
           )

            if(!isPlaylistsVisible) {
                bind.rvPlaylists.slideAnim(800, 100, R.anim.slide_from_above_anim)

            } else {
                bind.rvPlaylists.slideAnim(500, 0, R.anim.slide_up_away_anim)
            }
        }

    }


    private fun setArrowToFavClickListener(){

        bind.ivArrowBackToFav.setOnClickListener {

            playlistAdapter.unselectPlaylist()

            mainAdapter.animator.resetAnimator()

            databaseViewModel.getAllFavouredStations()

//            bind.rvFavStations.apply {
//                post {
//                    scheduleLayoutAnimation()
//                }
//            }
        }
    }



    private fun setPlaylistAdapterClickListeners(){

        playlistAdapter.apply {

            setAddPlaylistClickListener {
                CreatePlaylistDialog(
                    requireContext(), listOfPlaylists, databaseViewModel, pixabayViewModel, glide, null
                ).show()
            }


            setLazyListClickListener {

                playlistAdapter.unselectPlaylist()

                mainAdapter.animator.resetAnimator()

                databaseViewModel.getLazyPlaylist()

            }


            setPlaylistClickListener { playlist, position ->

                if(
                   currentTab != SEARCH_FROM_FAVOURITES && playlist.playlistName == currentPlaylistName
                        ) {/*DO NOTHING*/ }
                else {
                    mainAdapter.animator.resetAnimator()
                    databaseViewModel.subscribeToStationsInPlaylist(playlist.playlistName)
                    currentPlaylistPosition = position

//                    bind.rvFavStations.post {
//                        bind.rvFavStations.scheduleLayoutAnimation()
//                    }
                }
            }

            handleDragAndDrop = { stationID, playlistName ->

                if(currentTab == SEARCH_FROM_FAVOURITES){
                    if(RadioService.currentMediaItems == SEARCH_FROM_FAVOURITES){

                        mainViewModel.removeMediaItem(dragAndDropItemPos)
                    }
                    databaseViewModel.updateIsFavouredState(0, stationID)

                }
                    else {

                    if(currentPlaylistName == RadioService.currentPlaylistName){
                        mainViewModel.removeMediaItem(dragAndDropItemPos)
                    }

                    databaseViewModel.deleteStationPlaylistCrossRef(
                        stationID, currentPlaylistName)
                    }

                insertStationInPlaylist(stationID, playlistName)

            }
        }


    }

    private fun subscribeToObservers(){

        observeListOfPlaylists()


        databaseViewModel.currentPlaylistName.observe(viewLifecycleOwner){

            bind.tvPlaylistName.text = it
            currentPlaylistName = it
            playlistAdapter.currentPlaylistName = it

        }

        observeFavOrPlaylistState()

        observeStations()

//        observeUnfilteredPlaylist()

    }

    private fun observeStations(){


        lifecycleScope.launch {
            databaseViewModel.favFragStationsFlow.collectLatest {

                withContext(Dispatchers.Main){
                    bind.tvPlaylistMessage.apply {
                        if(it.isEmpty()){
                            visibility = View.VISIBLE
                            slideAnim(400, 0, R.anim.fade_in_anim)
                        }
                        else {
                            visibility = View.INVISIBLE
                        }
                    }
                }

                mainAdapter.listOfStations = it

            }
        }


//        databaseViewModel.observableListOfStations.observe(viewLifecycleOwner){


//            bind.tvPlaylistMessage.apply {
//                if(it.isEmpty()){
//                    visibility = View.VISIBLE
//                    slideAnim(400, 0, R.anim.fade_in_anim)
//                }
//                else {
//                    visibility = View.INVISIBLE
//                }
//            }

//            mainAdapter.listOfStations = it

//        }
    }

//    private fun observeUnfilteredPlaylist(){
//        databaseViewModel.stationsInPlaylist.observe(viewLifecycleOwner){ playlist ->
//            playlist?.radioStations?.let { stations ->
//                sortStationsInPlaylist(stations)
//               }
//            }
//        }


//    private fun sortStationsInPlaylist(stations : List<RadioStation>){
//
//        lifecycleScope.launch {
//
//            val result: MutableList<RadioStation> = mutableListOf()
//
//            val stationIndexMap = stations.withIndex().associate { it.value.stationuuid to it.index }
//
//            val order = databaseViewModel.getPlaylistOrder(currentPlaylistName)
//
//            order.forEach { crossref ->
//                val index = stationIndexMap[crossref.stationuuid]
//                if (index != null) {
//                    result.add(stations[index])
//                }
//            }
//            databaseViewModel.playlist.postValue(result)
//        }
//    }


    private fun observeFavOrPlaylistState(){

        databaseViewModel.favFragStationsSwitch.observe(viewLifecycleOwner){

            updateUiOnTabChange(it)

            currentTab = it

            playlistAdapter.currentTab = it

            searchFlag = it

        }
    }


    private fun updateUiOnTabChange(newTab : Int){

        bind.ivArrowBackToFav.isVisible = newTab != SEARCH_FROM_FAVOURITES && isPlaylistsVisible

        if(newTab == SEARCH_FROM_FAVOURITES){

            bind.tvPlaylistName.text = ""

            (bind.tvPlaylistEdit as TextView).text = ""

            bind.tvFavouredTitle.text = "Favoured"


            bind.tvPlaylistsExpand.updateLayoutParams<ConstraintLayout.LayoutParams> {
                startToStart = bind.guidelineMiddle.id
                endToEnd = bind.clFavFrag.id
                topToTop = bind.clFavFrag.id

            }

            bind.separatorLeft?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                startToStart = bind.clFavFrag.id
                endToEnd = bind.clFavFrag.id
                topToTop = bind.clFavFrag.id
            }

            bind.separatorRight?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                startToStart = bind.clFavFrag.id
                endToEnd = bind.clFavFrag.id
                topToTop = bind.clFavFrag.id
            }

            setToolbar(true)

        } else if(currentTab + newTab != SEARCH_FROM_PLAYLIST + SEARCH_FROM_LAZY_LIST){

            bind.tvFavouredTitle.text = ""
            bind.tvPlaylistsExpand.updateLayoutParams<ConstraintLayout.LayoutParams> {
                startToStart = bind.guideline66.id
                endToEnd = bind.clFavFrag.id
                topToTop = bind.clFavFrag.id
            }

            bind.separatorLeft?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                startToStart = bind.guideline33.id
                endToEnd = bind.guideline33.id
                topToTop = bind.clFavFrag.id
            }

            bind.separatorRight?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                startToStart = bind.guideline66.id
                endToEnd = bind.guideline66.id
                topToTop = bind.clFavFrag.id
            }

            setToolbar(false)
        }

        if(newTab == SEARCH_FROM_LAZY_LIST){
            (bind.tvPlaylistEdit as TextView).text = "Export"
        } else if(newTab == SEARCH_FROM_PLAYLIST) {
            (bind.tvPlaylistEdit as TextView).text = "Edit"
        }

    }

    private fun setToolbar(isInFav : Boolean){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){

            if(isInFav)
                bind.viewToolbar.setBackgroundResource(R.drawable.toolbar_fav_vector)

            else
                bind.viewToolbar.setBackgroundResource(R.drawable.toolbar_playlists_vector)


            val color = ContextCompat.getColor(requireContext(), R.color.nav_bar_fav_fragment)
//            val statusColor = ContextCompat.getColor(requireContext(), R.color.status_bar_fav_fragment)

            (activity as MainActivity).apply {
                window.navigationBarColor = color
                window.statusBarColor = color
            }
        }
    }



    private fun observeListOfPlaylists (){

        databaseViewModel.listOfAllPlaylists.observe(viewLifecycleOwner){ originalList ->

            val listWithHeader = mutableListOf(Playlist("", ""))

            listWithHeader.addAll(originalList)

            playlistAdapter.differ.submitList(listWithHeader)

            listOfPlaylists = originalList

        }
    }

    private fun setMainAdapterClickListener(){

        mainAdapter.utils.setOnClickListener { station, position ->

            var isToChangeMediaItems = false


           // When click on station from playlist and before that was another playlist
            if(currentPlaylistName != RadioService.currentPlaylistName && currentTab == SEARCH_FROM_PLAYLIST) {
                RadioSource.updatePlaylistStations(mainAdapter.listOfStations)
                RadioService.currentPlaylistName = currentPlaylistName
                isToChangeMediaItems = true
            }
                // When flag changed
            else if(searchFlag != RadioService.currentMediaItems)
                    isToChangeMediaItems = true


            mainViewModel.playOrToggleStation(station, searchFlag, itemIndex = position, isToChangeMediaItems =
            isToChangeMediaItems)
        }
    }

    private fun setupMainRecycleView(){

        bind.rvFavStations.apply {

            layoutManager = LinearLayoutManager(requireContext())
            adapter = mainAdapter
            edgeEffectFactory = BounceEdgeEffectFactory()
            setHasFixedSize(true)
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)

            setAdapterValues(mainAdapter.utils)


            if(RadioService.currentMediaItems != SEARCH_FROM_RECORDINGS){
                RadioService.currentPlayingStation.value?.let {
                    val id =  it.stationuuid
                    mainAdapter.currentRadioStationId = id
                }
            } else{
                mainAdapter.currentRadioStationId = ""
            }


//            layoutAnimation = (activity as MainActivity).layoutAnimationController

                post {

                    val index = getCurrentItemPosition(RadioService.currentPlayingStation.value)

                    if(index != -1){
                        scrollToPosition(index)
                    }

//                    scheduleLayoutAnimation()
                }

            addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if(newState == SCROLL_STATE_DRAGGING)
                        mainAdapter.animator.cancelAnimator()
                }
            })

        }
    }

    private fun setupPlaylistRecycleView(){

        playlistAdapter.apply {
            strokeWidth = 2f.dpToP(requireContext())
            strokeColor = ContextCompat.getColor(requireContext(), R.color.recording_seekbar_progress)
        }

        bind.rvPlaylists.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
        }
    }


    private val itemTouchCallback by lazy {

        object : SwipeToDeleteCallback(requireContext())

        {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition
                val stationID = mainAdapter.listOfStations[position].stationuuid
                val favouredAt =  mainAdapter.listOfStations[position].favouredAt
                if(currentTab == SEARCH_FROM_FAVOURITES){
                    handleSwipeOnFavStation(stationID, favouredAt, viewHolder.absoluteAdapterPosition)
                } else if(currentTab == SEARCH_FROM_PLAYLIST){
                    handleSwipeOnPlaylistStation(stationID, viewHolder.absoluteAdapterPosition)
                } else {

                }
            }
        }
    }

    private fun handleSwipeOnFavStation(stationID : String, favouredAt : Long, pos : Int){

        if(RadioService.currentMediaItems == SEARCH_FROM_FAVOURITES){
           mainViewModel.removeMediaItem(pos)
        }


        databaseViewModel.updateIsFavouredState(0, stationID).also{
            Snackbar.make(
                requireActivity().findViewById(R.id.rootLayout),
                "Station removed from favs", Snackbar.LENGTH_LONG
            ).apply {

                setAction("UNDO"){
                    databaseViewModel.updateIsFavouredState(favouredAt, stationID)

                    if(RadioService.currentMediaItems == SEARCH_FROM_FAVOURITES){
                       mainViewModel.restoreMediaItem(pos)
                    }


                }
            }.show()
        }
    }



    private fun handleSwipeOnPlaylistStation(stationID : String, pos : Int){

        if(RadioService.currentMediaItems == SEARCH_FROM_PLAYLIST
            && currentPlaylistName == RadioService.currentPlaylistName){
            mainViewModel.removeMediaItem(pos)
        }


        val playlistName = currentPlaylistName
        lifecycleScope.launch {
            val timeOfInsertion = databaseViewModel.getTimeOfStationPlaylistInsertion(stationID, playlistName)
            databaseViewModel.deleteStationPlaylistCrossRef(stationID, playlistName)
            withContext(Dispatchers.Main){
                Snackbar.make(
                    requireActivity().findViewById(R.id.rootLayout),
                    "Station removed from $playlistName", Snackbar.LENGTH_LONG
                ).apply {
                    setAction("UNDO"){

                        if(RadioService.currentMediaItems == SEARCH_FROM_PLAYLIST
                            && currentPlaylistName == RadioService.currentPlaylistName){
                            mainViewModel.restoreMediaItem(pos)
                        }
                        databaseViewModel.insertStationPlaylistCrossRef(
                            StationPlaylistCrossRef(
                                stationID, playlistName, timeOfInsertion
                            )
                        )
                    }


                }.show()
            }
        }
    }


    private fun insertStationInPlaylist(stationID: String, playlistName : String){

            databaseViewModel.checkAndInsertStationPlaylistCrossRef(
                stationID, playlistName
            ) {

                val message = if(it) "Already in $playlistName"
                else "Station moved to $playlistName"

                Snackbar.make((activity as MainActivity).findViewById(R.id.rootLayout),
                    message, Snackbar.LENGTH_SHORT).show()

                if(!it && RadioService.currentPlaylistName == currentPlaylistName &&
                        RadioService.currentMediaItems == SEARCH_FROM_PLAYLIST){
                    databaseViewModel.addMediaItemOnDropToPlaylist()
                }


            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind.rvFavStations.adapter = null
        bind.rvPlaylists.adapter = null
        _bind = null
        isToHandleNewStationObserver = false
        mainAdapter.animator.resetAnimator()
    }

}