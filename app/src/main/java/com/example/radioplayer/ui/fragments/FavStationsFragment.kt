package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavStationsFragment : Fragment() {

    lateinit var bind : FragmentFavStationsBinding
    lateinit var databaseViewModel : DatabaseViewModel
    lateinit var mainViewModel: MainViewModel
    lateinit var playlistAdapter : PlaylistsAdapter
    lateinit var currentPlaylistName : String
    private lateinit var listOfPlaylists : List<Playlist>

    @Inject
    lateinit var mainAdapter: RadioDatabaseAdapter


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




        setupMainRecycleView()

        setupPlaylistRecycleView()

        subscribeToObservers()

        setMainAdapterClickListener()

        setPlaylistAdapterClickListeners()

        setArrowToFavClickListener()

    }



    private fun setArrowToFavClickListener(){

        bind.ivArrowBackToFav.setOnClickListener {

            databaseViewModel.getAllFavouredStations()

            databaseViewModel.testGetAllOneTimePlaylistStations()
        }
    }

    private fun setPlaylistAdapterClickListeners(){

        playlistAdapter.setAddPlaylistClickListener {
            CreatePlaylistDialog(requireContext(), listOfPlaylists, databaseViewModel).show()
        }

        playlistAdapter.setPlaylistClickListener {

            databaseViewModel.getStationsInPlaylist(it.playlistName)

        }

        playlistAdapter.setDeletePlaylistClickListener {

            RemovePlaylistDialog(requireContext(), listOfPlaylists) { playlistName ->
                databaseViewModel.deleteStationsFromPlaylist(playlistName)
                databaseViewModel.deletePlaylist(Playlist(playlistName))

            }.show()

        }

    }

    private fun subscribeToObservers(){

        observeListOfPlaylists()

        observeFavOrPlaylistStateForUI()

        databaseViewModel.currentPlaylistName.observe(viewLifecycleOwner){

            bind.tvPlaylistName.text = it
            currentPlaylistName = it

        }

        databaseViewModel.observableListOfStations.observe(viewLifecycleOwner){

            mainAdapter.listOfStations = it
        }

    }

    private fun observeFavOrPlaylistStateForUI(){

        databaseViewModel.isInFavouriteTab.observe(viewLifecycleOwner){

            if(it){
                bind.tvPlaylistName.text = "Favoured stations"
                bind.ivArrowBackToFav.visibility = View.GONE
            } else{

                bind.ivArrowBackToFav.isVisible = true

            }
        }

    }


    private fun observeListOfPlaylists (){

        databaseViewModel.listOfAllPlaylists.observe(viewLifecycleOwner){

            playlistAdapter.differ.submitList(it)

            listOfPlaylists = it

            playlistAdapter.footerDeletePlaylist?.itemView?.isVisible = it.isNotEmpty()

        }

    }

    private fun setMainAdapterClickListener(){

        mainAdapter.setOnClickListener {

            mainViewModel.playOrToggleStation(it, true)
            mainViewModel.newRadioStation.postValue(it)
            databaseViewModel.isStationInDB.postValue(true)

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

        playlistAdapter = PlaylistsAdapter()

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