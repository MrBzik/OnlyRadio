package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.example.radioplayer.adapters.RadioAdapter
import com.example.radioplayer.databinding.FragmentRadioSearchBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RadioSearchFragment : Fragment() {

    lateinit var bind : FragmentRadioSearchBinding

    lateinit var viewModel : MainViewModel

    @Inject
    lateinit var radioAdapter : RadioAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        bind = FragmentRadioSearchBinding.inflate(inflater, container, false)

        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        setRecycleView()

        subscribeToObservers()

        radioAdapter.setOnClickListener {

            viewModel.playOrToggleStation(it, true)
        }

        bind.btnSearch.setOnClickListener{

            viewModel.searchWithNewTag("pop")

        }


    }

    private fun subscribeToObservers(){

        viewModel.mediaItems.observe(viewLifecycleOwner){

            when(it.status){
                Status.SUCCESS -> {
                   bind.allSongsProgressBar.isVisible = false
                    it.data?.let { stations ->
                        radioAdapter.listOfStations = stations
                    }
                }
                Status.ERROR -> Unit

                Status.LOADING -> bind.allSongsProgressBar.isVisible = true

            }
        }
    }

    private fun setRecycleView(){

        bind.rvSearchStations.apply {

            adapter = radioAdapter
            layoutManager = LinearLayoutManager(requireContext())

        }


    }


}