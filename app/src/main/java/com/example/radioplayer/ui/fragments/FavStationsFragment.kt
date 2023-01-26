package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.InvalidationTracker
import com.example.radioplayer.R
import com.example.radioplayer.adapters.RadioDatabaseAdapter
import com.example.radioplayer.databinding.FragmentFavStationsBinding
import com.example.radioplayer.databinding.FragmentRadioSearchBinding
import com.example.radioplayer.ui.MainActivity
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

    @Inject
    lateinit var dbAdapter: RadioDatabaseAdapter


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

        setupRecycleView()




        dbAdapter.setOnClickListener {

            mainViewModel.playOrToggleStation(it)
            mainViewModel.newRadioStation.postValue(it)

        }

        databaseViewModel.getAllStationsTEST.observe(viewLifecycleOwner){
            dbAdapter.listOfStations = it
        }

    }

    private fun setupRecycleView(){

        bind.rvFavStations.apply {

            layoutManager = LinearLayoutManager(requireContext())
            adapter = dbAdapter

//            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)

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
            val item = dbAdapter.listOfStations[position]

            databaseViewModel.deleteStation(item).also {

                Snackbar.make(
                    requireActivity().findViewById(R.id.rootLayout),
                    "Station removed from favs", Snackbar.LENGTH_LONG
                    ).apply {

                        setAction("UNDO"){
                            databaseViewModel.insertRadioStation(item)

                        }
                }.show()
            }

        }


    }



}