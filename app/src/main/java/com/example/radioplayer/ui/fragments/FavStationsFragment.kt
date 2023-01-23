package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.RadioDatabaseAdapter
import com.example.radioplayer.databinding.FragmentFavStationsBinding
import com.example.radioplayer.databinding.FragmentRadioSearchBinding
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
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

        activity?.title = "Here are your favourite stations"

        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        databaseViewModel = (activity as MainActivity).databaseViewModel
        mainViewModel = (activity as MainActivity).mainViewModel

        setupRecycleView()

        databaseViewModel.radioStations.observe(viewLifecycleOwner){

            it?.let {
                dbAdapter.listOfStations = it
            }
        }

         databaseViewModel.getAllItems()

        dbAdapter.setOnClickListener {

            mainViewModel.playOrToggleStation(it)

        }

    }

    private fun setupRecycleView(){

        bind.rvFavStations.apply {

            adapter = dbAdapter
            layoutManager = LinearLayoutManager(requireContext())

        }
    }



}