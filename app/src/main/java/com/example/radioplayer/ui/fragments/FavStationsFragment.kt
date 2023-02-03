package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PlaylistsAdapter
import com.example.radioplayer.adapters.RadioDatabaseAdapter
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.databinding.FragmentFavStationsBinding
import com.example.radioplayer.ui.dialogs.CreatePlaylistDialog
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.dialogs.RemovePlaylistDialog
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.ui.viewmodels.PixabayViewModel
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavStationsFragment : Fragment() {

    lateinit var bind : FragmentFavStationsBinding
    lateinit var databaseViewModel : DatabaseViewModel
    lateinit var mainViewModel: MainViewModel
    private var currentPlaylistName : String = ""
    private lateinit var listOfPlaylists : List<Playlist>
    lateinit var pixabayViewModel: PixabayViewModel

    private var isInFavouriteTab = false

    private var isPlaylistsVisible = false

    private var searchFlag : Int = 1

    @Inject
    lateinit var mainAdapter: RadioDatabaseAdapter

    @Inject
    lateinit var playlistAdapter : PlaylistsAdapter

    @Inject
    lateinit var glide : RequestManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        bind = FragmentFavStationsBinding.inflate(inflater, container, false)

        return bind.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseViewModel = (activity as MainActivity).databaseViewModel
        mainViewModel = (activity as MainActivity).mainViewModel
        pixabayViewModel = ViewModelProvider(requireActivity())[PixabayViewModel::class.java]



        setupMainRecycleView()

        setupPlaylistRecycleView()

        subscribeToObservers()

        setMainAdapterClickListener()

        setPlaylistAdapterClickListeners()

        setArrowToFavClickListener()

        setOnPlaylistsExpandClickListener()

        observePlaylistsVisibilityState()

        deletePlaylistClickListener()

    }

    private fun deletePlaylistClickListener(){

        bind.tvPlaylistDelete.setOnClickListener {

            RemovePlaylistDialog(requireContext(), currentPlaylistName){

               val stations = mainAdapter.listOfStations

                databaseViewModel.deletePlaylistAndContent(currentPlaylistName, stations)

                databaseViewModel.getAllFavouredStations()


            }.show()

        }

    }

    private fun observePlaylistsVisibilityState(){

        pixabayViewModel.togglePlaylistsVisibility.observe(viewLifecycleOwner){
            bind.rvPlaylists.isVisible = it
            isPlaylistsVisible = it
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

        }
    }


    private fun setArrowToFavClickListener(){

        bind.ivArrowBackToFav.setOnClickListener {

            databaseViewModel.getAllFavouredStations()

        }
    }

    private fun setPlaylistAdapterClickListeners(){

        playlistAdapter.setAddPlaylistClickListener {
           CreatePlaylistDialog(
                requireContext(), listOfPlaylists, databaseViewModel, pixabayViewModel, glide).show()

        }

        playlistAdapter.setPlaylistClickListener {

            databaseViewModel.getStationsInPlaylist(it.playlistName)

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

            bind.tvPlaylistDelete.isVisible = !it
            bind.tvPlaylistDelete.text = "Delete : $currentPlaylistName"

            isInFavouriteTab = it


            if(it){
                bind.tvPlaylistName.text = "Favoured stations"
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
            mainViewModel.newRadioStation.postValue(it)
            databaseViewModel.isStationInDB.postValue(true)
            databaseViewModel.checkDateAndUpdateHistory(it.stationuuid)
        }
    }

    private fun setupMainRecycleView(){

        bind.rvFavStations.apply {

            layoutManager = LinearLayoutManager(requireContext())
            adapter = mainAdapter

            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)

        }
    }

    private fun setupPlaylistRecycleView(){

        bind.rvPlaylists.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

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
                    databaseViewModel.updateIsFavouredState(1, stationID)
                }
            }.show()
        }
    }

    private fun handleSwipeOnPlaylistStation(stationID : String){

        databaseViewModel.deleteStationPlaylistCrossRef(
            StationPlaylistCrossRef(stationID, currentPlaylistName)
        )
        databaseViewModel.decrementRadioStationPlaylist(stationID)

        databaseViewModel.getStationsInPlaylist(currentPlaylistName, true)

        Snackbar.make(
            requireActivity().findViewById(R.id.rootLayout),
            "Station removed from $currentPlaylistName", Snackbar.LENGTH_LONG
        ).apply {
            setAction("UNDO"){

                databaseViewModel.insertStationPlaylistCrossRef(
                    StationPlaylistCrossRef(stationID, currentPlaylistName)
                )
                databaseViewModel.incrementRadioStationPlaylist(stationID)

                databaseViewModel.getStationsInPlaylist(currentPlaylistName, true)
            }
        }.show()
    }

}