package com.example.radioplayer.ui.fragments


import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PlaylistsAdapter
import com.example.radioplayer.adapters.RadioDatabaseAdapter
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.databinding.FragmentFavStationsBinding
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.dialogs.CreatePlaylistDialog
import com.example.radioplayer.ui.dialogs.EditPlaylistDialog
import com.example.radioplayer.ui.dialogs.RemovePlaylistDialog
import com.example.radioplayer.ui.viewmodels.PixabayViewModel
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavStationsFragment : BaseFragment<FragmentFavStationsBinding>(
    FragmentFavStationsBinding::inflate
) {


    private var currentPlaylistName : String = ""
    private lateinit var listOfPlaylists : List<Playlist>
    lateinit var pixabayViewModel: PixabayViewModel

    private var isInFavouriteTab = false

    private var isPlaylistsVisible = false

    private var searchFlag : Int = 1

    @Inject
    lateinit var mainAdapter: RadioDatabaseAdapter

    lateinit var playlistAdapter : PlaylistsAdapter

    @Inject
    lateinit var glide : RequestManager

    private var currentPlaylistPosition = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pixabayViewModel = ViewModelProvider(requireActivity())[PixabayViewModel::class.java]

        setupMainRecycleView()

        setupPlaylistRecycleView()

        observePlaybackState()

        subscribeToObservers()

        setMainAdapterClickListener()

        setPlaylistAdapterClickListeners()

        setArrowToFavClickListener()

        setOnPlaylistsExpandClickListener()

        observePlaylistsVisibilityState()

        editPlaylistClickListener()

        endLoadingBarIfNeeded()

    }


    private fun observePlaybackState(){
        mainViewModel.playbackState.observe(viewLifecycleOwner){
            it?.let {

                when{
                    it.isPlaying -> {
                        mainAdapter.currentPlaybackState = true
                        mainAdapter.updateStationPlaybackState()
                    }
                    it.isPlayEnabled -> {
                        mainAdapter.currentPlaybackState = false
                        mainAdapter.updateStationPlaybackState()
                    }
                }
            }
        }
    }

    private fun endLoadingBarIfNeeded(){
        (activity as MainActivity).separatorLeftAnim.endLoadingAnim()
        (activity as MainActivity).separatorRightAnim.endLoadingAnim()
    }

    private fun editPlaylistClickListener(){

        bind.tvPlaylistEdit.setOnClickListener {

            var isDeletePlaylistCalled = false
            var isCoverUpdateNeeded = false
            var newImageUrl = ""

            val dialog =  EditPlaylistDialog (
                requireContext(), listOfPlaylists,
                currentPlaylistName,
                currentPlaylistPosition,
                databaseViewModel, pixabayViewModel, glide,
             {
                isDeletePlaylistCalled = it
            }, { checkCoverUpdate, imageUrl ->
                isCoverUpdateNeeded = checkCoverUpdate
                newImageUrl = imageUrl
                })

            dialog.show()

            dialog.setOnDismissListener {

                if(isCoverUpdateNeeded){


                    val view = bind.rvPlaylists
                        .findViewHolderForAdapterPosition(currentPlaylistPosition)?.itemView?.
                        findViewById<ImageView>(R.id.ivPlaylistCover)

                    view?.let {
                        glide
                            .load(newImageUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(it)
                    }
                }

                if(isDeletePlaylistCalled){

                    RemovePlaylistDialog(requireContext(), currentPlaylistName){

                        databaseViewModel.deletePlaylistAndContent(currentPlaylistName)

                        databaseViewModel.getAllFavouredStations()

                    }.show()
                }
            }
        }
    }


    private fun observePlaylistsVisibilityState(){

        pixabayViewModel.togglePlaylistsVisibility.observe(viewLifecycleOwner){

            isPlaylistsVisible = it

            bind.rvPlaylists.isVisible = it

            bind.ivArrowPlaylistsShrink.isVisible = it
            bind.ivArrowPlaylistsExpand.isVisible = !it
            bind.ivArrowBackToFav.isVisible = it && !isInFavouriteTab

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

            databaseViewModel.getAllFavouredStations()

        }
    }

    private fun setPlaylistAdapterClickListeners(){

        playlistAdapter.apply {

            setAddPlaylistClickListener {
                CreatePlaylistDialog(
                    requireContext(), listOfPlaylists, databaseViewModel, pixabayViewModel, glide, null
                ).show()
            }

            setPlaylistClickListener { playlist, position ->
                databaseViewModel.subscribeToStationsInPlaylist(playlist.playlistName)
                currentPlaylistPosition = position
            }

            handleDragAndDrop = { stationID, playlistName ->

                if(isInFavouriteTab){

                    databaseViewModel.updateIsFavouredState(0, stationID)

                }
                    else {
                    databaseViewModel.deleteStationPlaylistCrossRef(StationPlaylistCrossRef(
                        stationID, currentPlaylistName))

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
        }

        observeFavOrPlaylistState()

        databaseViewModel.observableListOfStations.observe(viewLifecycleOwner){

            mainAdapter.listOfStations = it

        }

    }

    private fun observeFavOrPlaylistState(){

        databaseViewModel.isInFavouriteTab.observe(viewLifecycleOwner){

            bind.ivArrowBackToFav.isVisible = !it && isPlaylistsVisible

            bind.tvPlaylistEdit.isVisible = !it


            isInFavouriteTab = it


            if(it){
                bind.tvPlaylistName.text = getString(R.string.Favoured)
                searchFlag = SEARCH_FROM_FAVOURITES

            } else{

                searchFlag = SEARCH_FROM_PLAYLIST
            }
        }

    }


    private fun observeListOfPlaylists (){

        databaseViewModel.listOfAllPlaylists.observe(viewLifecycleOwner){

            playlistAdapter.differ.submitList(it)

            listOfPlaylists = it


        }

    }

    private fun setMainAdapterClickListener(){

        mainAdapter.setOnClickListener {

            mainViewModel.playOrToggleStation(it, searchFlag)
            databaseViewModel.checkDateAndUpdateHistory(it.stationuuid)
        }
    }

    private fun setupMainRecycleView(){

        bind.rvFavStations.apply {

            layoutManager = LinearLayoutManager(requireContext())
            adapter = mainAdapter
            edgeEffectFactory = BounceEdgeEffectFactory()
            setHasFixedSize(true)
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)

            mainAdapter.apply {
                defaultTextColor = ContextCompat.getColor(requireContext(), R.color.default_text_color)
                selectedTextColor = ContextCompat.getColor(requireContext(), R.color.selected_text_color)
            }

            mainViewModel.currentRadioStation.value?.let {
                val name =  it.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                mainAdapter.currentRadioStationName = name
            }

            layoutAnimation = (activity as MainActivity).layoutAnimationController

                post {
                    scheduleLayoutAnimation()
                }
        }
    }

    private fun setupPlaylistRecycleView(){

        playlistAdapter = PlaylistsAdapter(glide, true)

        bind.rvPlaylists.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
        }
    }


    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val stationID = mainAdapter.listOfStations[position].stationuuid

            if(databaseViewModel.isInFavouriteTab.value!!){
                handleSwipeOnFavStation(stationID)
            } else{
                handleSwipeOnPlaylistStation(stationID)
            }
        }
    }

    private fun handleSwipeOnFavStation(stationID : String){

        databaseViewModel.updateIsFavouredState(0, stationID).also{
            Snackbar.make(
                requireActivity().findViewById(R.id.rootLayout),
                "Station removed from favs", Snackbar.LENGTH_LONG
            ).apply {
                setAction("UNDO"){
                    databaseViewModel.updateIsFavouredState(System.currentTimeMillis(), stationID)
                }
            }.show()
        }
    }

    private fun handleSwipeOnPlaylistStation(stationID : String){

        databaseViewModel.deleteStationPlaylistCrossRef(
            StationPlaylistCrossRef(stationID, currentPlaylistName)
        )

        Snackbar.make(
            requireActivity().findViewById(R.id.rootLayout),
            "Station removed from $currentPlaylistName", Snackbar.LENGTH_LONG
        ).apply {
            setAction("UNDO"){

                databaseViewModel.insertStationPlaylistCrossRefAndUpdate(
                    StationPlaylistCrossRef(stationID, currentPlaylistName),
                    currentPlaylistName
                )
            }
        }.show()
    }


    private fun insertStationInPlaylist(stationID: String, playlistName : String){

            databaseViewModel.insertStationPlaylistCrossRefAndUpdate(
                StationPlaylistCrossRef(
                    stationID, playlistName
                ), currentPlaylistName
            )

            Snackbar.make((activity as MainActivity).findViewById(R.id.rootLayout),
                "Station was moved to $playlistName",
                Snackbar.LENGTH_SHORT).show()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind.rvFavStations.adapter = null
        bind.rvPlaylists.adapter = null
        _bind = null
    }

}