package com.onlyradio.radioplayer.ui.fragments


import android.content.res.Configuration
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.adapters.PlaylistsAdapter
import com.onlyradio.radioplayer.adapters.RadioDatabaseAdapter
import com.onlyradio.radioplayer.data.local.entities.Playlist
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.databinding.FragmentFavStationsBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.exoPlayer.RadioSource
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.onlyradio.radioplayer.ui.animations.SwipeToDeleteCallback
import com.onlyradio.radioplayer.ui.animations.slideAnim
import com.onlyradio.radioplayer.ui.dialogs.AddStationToPlaylistDialog
import com.onlyradio.radioplayer.ui.dialogs.CreatePlaylistDialog
import com.onlyradio.radioplayer.ui.dialogs.EditPlaylistDialog
import com.onlyradio.radioplayer.ui.dialogs.RemovePlaylistDialog
import com.onlyradio.radioplayer.ui.viewmodels.PixabayViewModel
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_LAZY_LIST
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.onlyradio.radioplayer.utils.dpToP
import com.google.android.material.snackbar.Snackbar
import com.onlyradio.radioplayer.domain.PlayingStationState
import com.onlyradio.radioplayer.extensions.observeFlow
import com.onlyradio.radioplayer.extensions.snackbarSimple
import com.onlyradio.radioplayer.utils.Constants
import com.onlyradio.radioplayer.utils.Constants.CLICK_DEBOUNCE
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
class FavStationsFragment : BaseFragment<FragmentFavStationsBinding>(
    FragmentFavStationsBinding::inflate
) {


    private var currentPlaylistName : String = ""
    private lateinit var listOfPlaylists : List<Playlist>
    private lateinit var pixabayViewModel: PixabayViewModel

//    private var isInFavouriteTab = false

    private var currentTab = SEARCH_FROM_FAVOURITES

    private var isPlaylistsVisible = false


    @Inject
    lateinit var mainAdapter: RadioDatabaseAdapter

    private val playlistAdapter : PlaylistsAdapter by lazy {
        PlaylistsAdapter(glide, true)
    }

    @Inject
    lateinit var glide : RequestManager

    private var currentPlaylistPosition = 0

    private var isOnViewCreated = false

    private var currentStationId = ""
    companion object{

        var dragAndDropItemPos = -1
        var dragAndDropStation : RadioStation? = null

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isOnViewCreated = true

        pixabayViewModel = ViewModelProvider(requireActivity())[PixabayViewModel::class.java]

        setupMainRecycleView()

        setupPlaylistRecycleView()

        observeStationWithPlaybackState()

        subscribeToObservers()

        setMainAdapterClickListener()

        setArrowToFavClickListener()

        setOnPlaylistsExpandClickListener()

        observePlaylistsVisibilityState()

        editPlaylistClickListener()

        setOnSwipeDeleteHandlerFlow()

        setLazyListName()

    }


    private fun observeStationWithPlaybackState(){

        var isToHandleNewStationObserver = false

        observeFlow(mainViewModel.isPlayingFlow.combine(RadioService.currentPlayingStation.asFlow()){ isPlaying, station ->
            PlayingStationState(station?.stationuuid ?: "", isPlaying)
        }){ state ->

            if(RadioService.isFromRecording) return@observeFlow

            mainAdapter.updatePlaybackState(state.isPlaying)

            currentStationId = state.stationId

            val id = state.stationId
            val index = getCurrentItemPosition(id)

            Logger.log("ON NEW ST: $index")

            if(!isToHandleNewStationObserver){
                isToHandleNewStationObserver = true
                mainAdapter.updateSelectedItemValues(index, id)
                return@observeFlow
            }

            if(index >= 0){
                handleNewRadioStation(index, id)
            }
        }
    }


    private fun getCurrentItemPosition(
        stationId: String?,
        list : List<RadioStation> = mainAdapter.listOfStations
    ) : Int {
        return if(currentTab == RadioService.currentMediaItems && currentTab != SEARCH_FROM_PLAYLIST ||
            currentTab == RadioService.currentMediaItems && currentPlaylistName == RadioService.currentPlaylistName){
            mainViewModel.getPlayerCurrentIndex()
        }

        else
            list.indexOfFirst {
                it.stationuuid == stationId
            }
    }

    private fun handleNewRadioStation(position : Int, stationId : String){

        if(!mainAdapter.isSameId(stationId))
            bind.rvFavStations.smoothScrollToPosition(position)

        bind.rvFavStations.post {

            bind.rvFavStations
                .findViewHolderForAdapterPosition(position)?.let {
                    if(it is RadioDatabaseAdapter.RadioItemHolder)
                        mainAdapter.onNewPlayingItem(position, stationId, it)
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
                    favViewModel, pixabayViewModel, glide
                ) {
                    isDeletePlaylistCalled = it
                }

                dialog.show()

                dialog.setOnDismissListener {

                    if(isDeletePlaylistCalled){

                        RemovePlaylistDialog(requireContext(), currentPlaylistName){

                            favViewModel.deletePlaylistAndContent(currentPlaylistName)

                            favViewModel.getAllFavouredStations()

                        }.show()
                    }
                }
            } else if(currentTab == SEARCH_FROM_LAZY_LIST){

                AddStationToPlaylistDialog(
                    requireContext(), listOfPlaylists,
                     favViewModel, pixabayViewModel, glide,
                    resources.getString(R.string.export_into_playlists_title),
                ) { playlistName ->
                    favViewModel.exportStationFromLazyList(playlistName)
                }.show()

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

            favViewModel.getAllFavouredStations()

            bind.rvFavStations.apply {
                post {
                    scheduleLayoutAnimation()
                }
            }
        }
    }


    private fun setLazyListName(){
        favViewModel.setLazyListName(
            resources.getString(R.string.lazy_playlist_name)
        )
    }

    private fun setPlaylistAdapterClickListeners(){

        playlistAdapter.apply {

            setAddPlaylistClickListener {
                CreatePlaylistDialog(
                    requireContext(), listOfPlaylists, favViewModel, pixabayViewModel, glide, null
                ).show()
            }


            setLazyListClickListener {

                bind.rvFavStations.post {

                    bind.rvFavStations.scheduleLayoutAnimation()
                }

                favViewModel.getLazyPlaylist()

            }


            setPlaylistClickListener { playlist, position ->

                bind.rvFavStations.post {

                    bind.rvFavStations.scheduleLayoutAnimation()
                }

                favViewModel.subscribeToStationsInPlaylist(playlist.playlistName)
                currentPlaylistPosition = position

            }

            setDragAndDrop { stationID, playlistName ->

                favViewModel.onDragAndDropStation(
                    index = dragAndDropItemPos,
                    stationID = stationID,
                    targetPlaylistName = playlistName
                ){
                    val message = if(it) resources.getString(R.string.already_in_playlist) + " " + playlistName
                    else resources.getString(R.string.moved_to_playlist) + " " + playlistName

                    requireActivity().snackbarSimple(message)
                }
            }
        }
    }

    private fun subscribeToObservers(){

        observeListOfPlaylists()

        favViewModel.currentPlaylistName.observe(viewLifecycleOwner){

            bind.tvPlaylistName.apply {
                visibility = View.VISIBLE
                text = it
            }
            currentPlaylistName = it
            playlistAdapter.currentPlaylistName = it

        }

        observeFavOrPlaylistState()

        observeStations()

    }


    private fun initialRvAnimAndScroll(index: Int){

        bind.rvFavStations.apply {
            post {
                if(index != -1){
                    scrollToPosition(index)
                }

                scheduleLayoutAnimation()
            }
        }
    }

    private fun observeStations(){

        var isInitialLaunch = true

        observeFlow(favViewModel.favFragStationsFlow){
            mainAdapter.listOfStations = it
            val newIndex = getCurrentItemPosition(currentStationId, it)

            Logger.log("ON NEW ITEMS: $newIndex")

            mainAdapter.updateSelectedIndex(newIndex)

            if(isInitialLaunch){
                isInitialLaunch = false
                initialRvAnimAndScroll(newIndex)
            }


            withContext(Dispatchers.Main){
                bind.tvPlaylistMessage.apply {
                    if(it.isEmpty()){
                        text = requireContext().resources.getString(
                            when(favViewModel.favFragStationsSwitch.value){
                                SEARCH_FROM_FAVOURITES -> R.string.fav_frag_hint
                                SEARCH_FROM_PLAYLIST -> R.string.playlist__hint
                                else -> R.string.lazy_list_hint
                            }
                        )
                        visibility = View.VISIBLE
                        slideAnim(400, 0, R.anim.fade_in_anim)
                    }
                    else {
                        visibility = View.INVISIBLE
                    }
                }
            }
        }
    }


    private fun observeFavOrPlaylistState(){

        favViewModel.favFragStationsSwitch.observe(viewLifecycleOwner) {
            updateUiOnTabChange(it)

            currentTab = it

            playlistAdapter.currentTab = it

        }


    }


    private fun updateUiOnTabChange(newTab : Int){

        bind.ivArrowBackToFav.isVisible = newTab != SEARCH_FROM_FAVOURITES && isPlaylistsVisible

        if(newTab == SEARCH_FROM_FAVOURITES){

            bind.tvPlaylistName.visibility = View.INVISIBLE

            (bind.tvPlaylistEdit as TextView).text = ""

            bind.tvFavouredTitle.text = resources.getString(R.string.favourite_title)

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
            (bind.tvPlaylistEdit as TextView).text = resources.getString(R.string.export)
        } else if(newTab == SEARCH_FROM_PLAYLIST) {
            (bind.tvPlaylistEdit as TextView).text = resources.getString(R.string.edit)
        }

    }

    private fun setToolbar(isInFav : Boolean){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){

            if(isOnViewCreated){
                isOnViewCreated = false
                val drawableId = if(isInFav) R.drawable.toolbar_favourite else R.drawable.toolbar_playlists
                bind.viewToolbar.setBackgroundResource(drawableId)
            } else {
                val drawableId = if(isInFav) R.drawable.toolbar_animated_favourite else R.drawable.toolbar_animated_fav_playlists
                val drawable = ContextCompat.getDrawable(requireContext(), drawableId) as AnimatedVectorDrawable
                bind.viewToolbar.background = drawable
                drawable.start()
            }
        }
    }


    private fun observeListOfPlaylists (){

        favViewModel.listOfAllPlaylists.observe(viewLifecycleOwner){ originalList ->

            val listWithHeader = mutableListOf(Playlist(
                resources.getString(R.string.lazy_playlist_name)
                , ""))

            listWithHeader.addAll(originalList)

            playlistAdapter.differ.submitList(listWithHeader)

            listOfPlaylists = originalList

        }
    }

    private fun setMainAdapterClickListener(){

        var clickJob : Job? = null

        mainAdapter.setOnClickListener { station, position ->

            if(clickJob?.isActive == true) return@setOnClickListener

            clickJob = lifecycleScope.launch {

                mainViewModel.playOrToggleStation(
                    stationId = station.stationuuid,
                    searchFlag = currentTab,
                    itemIndex = position,
                    isToChangeMediaItems = favViewModel.isToChangeMediaItems()
                )
                delay(CLICK_DEBOUNCE)
            }
        }
    }

    private fun setupMainRecycleView(){

        bind.rvFavStations.apply {

            layoutManager = LinearLayoutManager(requireContext())
            adapter = mainAdapter
            edgeEffectFactory = BounceEdgeEffectFactory()
            setHasFixedSize(true)
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)

            mainAdapter.initialiseValues(requireContext(), settingsViewModel.stationsTitleSize)

            layoutAnimation = (activity as MainActivity).layoutAnimationController

        }
    }

    private fun setupPlaylistRecycleView(){

        playlistAdapter.apply {
            strokeWidth = 2f.dpToP(requireContext())
            strokeColor = ContextCompat.getColor(requireContext(), R.color.recording_seekbar_progress)
            setPlaylistAdapterClickListeners()
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
                val position = viewHolder.bindingAdapterPosition
                val stationID = mainAdapter.listOfStations[position].stationuuid

                favViewModel.onSwipeDeleteStation(position, stationID)
            }
        }
    }




    private fun setOnSwipeDeleteHandlerFlow() = viewLifecycleOwner.lifecycleScope.launch {

        repeatOnLifecycle(Lifecycle.State.STARTED){

            favViewModel.onSwipeDeleteHandled.collectLatest {

                val messageId = when(it.playlist){

                    SEARCH_FROM_FAVOURITES -> {
                        R.string.removed_from_favs
                    }

                    SEARCH_FROM_PLAYLIST -> {
                        R.string.removed_from_playlist
                    }

                    else -> {
                        favViewModel.getLazyPlaylist()
                        R.string.removed_from_lazy_list
                    }

                }

                var message = resources.getString(messageId)

                if(it.playlist == SEARCH_FROM_PLAYLIST) {
                    message = message + " " + it.playlistName
                }

                Snackbar.make(requireActivity().findViewById(R.id.rootLayout), message, Snackbar.LENGTH_LONG).apply {
                    setAction(resources.getString(R.string.action_undo)){
                        favViewModel.onRestoreStation()
                    }
                }.show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        bind.rvFavStations.adapter = null
        bind.rvPlaylists.adapter = null
        _bind = null
    }



}