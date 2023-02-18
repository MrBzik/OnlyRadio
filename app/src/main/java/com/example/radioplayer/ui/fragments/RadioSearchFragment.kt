package com.example.radioplayer.ui.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PagingRadioAdapter
import com.example.radioplayer.databinding.FragmentRadioSearchBinding
import com.example.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.dialogs.TagPickerDialog
import com.example.radioplayer.ui.dialogs.CountryPickerDialog
import com.example.radioplayer.ui.dialogs.NameDialog
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.listOfTags
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RadioSearchFragment : BaseFragment<FragmentRadioSearchBinding>(
    FragmentRadioSearchBinding::inflate
) {


    private val allTags = listOfTags

    @Inject
    lateinit var pagingRadioAdapter : PagingRadioAdapter




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSearchParamsObservers()

        setSearchToolbar()

        setRecycleView()

        setAdapterLoadStateListener()

        setAdapterOnClickListener()

        setOnRefreshSearch()

        listenSearchButton()

        subscribeToStationsFlow()

    }


    private fun setAdapterOnClickListener(){

        pagingRadioAdapter.setOnClickListener {

            mainViewModel.playOrToggleStation(it, SEARCH_FROM_API)
            mainViewModel.newRadioStation.postValue(it)
            databaseViewModel.insertRadioStation(it)
            databaseViewModel.checkDateAndUpdateHistory(it.stationuuid)

        }

    }


    private fun setRecycleView(){

        bind.rvSearchStations.apply {

            adapter = pagingRadioAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)

        }
    }

    private fun setAdapterLoadStateListener(){

        pagingRadioAdapter.addLoadStateListener {

            if (it.refresh is LoadState.Loading ||
                it.append is LoadState.Loading)
                bind.loadStationsProgressBar.isVisible = true


            else {
                bind.loadStationsProgressBar.visibility = View.GONE
            }

        }

    }


    private fun subscribeToStationsFlow(){

        viewLifecycleOwner.lifecycleScope.launch {

            mainViewModel.stationsFlow.collectLatest {

                pagingRadioAdapter.submitData(it)
            }
        }

    }



    private fun setSearchToolbar() {


        bind.tvTag.setOnClickListener {

          TagPickerDialog(requireContext(), allTags, mainViewModel).apply {
              show()



          }
        }


        bind.tvName.setOnClickListener {

            NameDialog(requireContext(), it as TextView, mainViewModel).show()

        }

        bind.tvSelectedCountry.setOnClickListener {

            CountryPickerDialog(requireContext(), mainViewModel).show()

        }

    }


    private fun setOnRefreshSearch(){

        bind.swipeRefresh.setOnRefreshListener {

            initiateNewSearch()

            bind.swipeRefresh.isRefreshing = false

        }
    }

    private fun listenSearchButton(){

        bind.ivInitiateSearch.setOnClickListener {
            initiateNewSearch()
        }

    }


    private fun initiateNewSearch(){

        val name = bind.tvName.text.toString()

        val tag = bind.tvTag.text.toString()

        val country = bind.tvSelectedCountry.text.toString()

        val bundle = Bundle().apply {


            if(tag == "Tag") {
                putString("TAG", "")
            } else {
                putString("TAG", tag)
            }

           if(country == "Country"){
               putString("COUNTRY", "")
           } else {
               putString("COUNTRY", country)
           }

            if(name == "Name"){
                putString("NAME", "")
            } else {
                putString("NAME", name)
            }

        }
        mainViewModel.isNewSearch = true
        mainViewModel.setSearchBy(bundle)

        bind.rvSearchStations.smoothScrollToPosition(0)


    }


    private fun setSearchParamsObservers(){

        mainViewModel.searchParamTag.observe(viewLifecycleOwner){

             bind.tvTag.text = if (it == "") "Tag" else it

        }

        mainViewModel.searchParamName.observe(viewLifecycleOwner){
            bind.tvName.text = if (it == "") "Name" else it
        }

        mainViewModel.searchParamCountry.observe(viewLifecycleOwner){

           bind.tvSelectedCountry.text = if (it == "") "Country" else it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind.rvSearchStations.adapter = null
        _bind = null
    }

}





