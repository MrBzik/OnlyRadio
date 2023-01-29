package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.PagingRadioAdapter
import com.example.radioplayer.databinding.FragmentRadioSearchBinding
import com.example.radioplayer.ui.dialogs.DialogPicker
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.dialogs.NameDialog
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.listOfTags
import com.hbb20.countrypicker.models.CPCountry
import com.hbb20.countrypicker.view.CPViewHelper
import com.hbb20.countrypicker.view.prepareCustomCountryPickerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RadioSearchFragment : Fragment() {

    lateinit var bind : FragmentRadioSearchBinding

    lateinit var mainViewModel : MainViewModel
    lateinit var databaseViewModel : DatabaseViewModel

    private var selectedCountry = ""

    private val allTags = listOfTags

    lateinit var  cpViewHelper : CPViewHelper

    @Inject
    lateinit var pagingRadioAdapter : PagingRadioAdapter

    private var isOnCreateCalled = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isOnCreateCalled = true
    }

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

        mainViewModel = (activity as MainActivity).mainViewModel
        databaseViewModel = (activity as MainActivity).databaseViewModel

        setSearchParamsObservers()

        setSearchToolbar()

        setRecycleView()

        setAdapterLoadStateListener()

        setAdapterOnClickListener()

        observeStationsForAdapter()

        setOnRefreshSearch()

        listenSearchButton()

    }


    private fun setAdapterOnClickListener(){

        pagingRadioAdapter.setOnClickListener {

            mainViewModel.playOrToggleStation(it, true)
            mainViewModel.newRadioStation.postValue(it)
            databaseViewModel.ifStationAlreadyInDatabase(it.stationuuid)

        }

    }


    private fun setRecycleView(){

        bind.rvSearchStations.apply {

            adapter = pagingRadioAdapter
            layoutManager = LinearLayoutManager(requireContext())

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

        bind.rvSearchStations

        }

    }


    private fun observeStationsForAdapter(){
        viewLifecycleOwner.lifecycleScope.launch{
            mainViewModel.stationsFlow.collectLatest {
                pagingRadioAdapter.submitData(it)
            }
        }
    }


    private fun setupCountryPicker() {

         cpViewHelper = requireContext().prepareCustomCountryPickerView(
            containerViewGroup = bind.llCountryViewGroup,
            tvSelectedCountryInfo = bind.tvSelectedCountry,
            allowClearSelection = true,
            selectedCountryInfoTextGenerator = {cpCountry: CPCountry ->  cpCountry.alpha2 }

        )


        mainViewModel.searchParamCountry.value?.let { code ->
            cpViewHelper.setCountryForAlphaCode(code)
        }

            cpViewHelper.selectedCountry.observe(viewLifecycleOwner){

                    it?.let { country ->
                        mainViewModel.searchParamCountry.postValue(country.alpha2)
                        isOnCreateCalled = false
                    } ?: run {
                        if (isOnCreateCalled) {
                            isOnCreateCalled = false
                        } else {
                            mainViewModel.searchParamCountry.postValue("")
                        }
                    }
            }


    }

    private fun setSearchToolbar() {


        bind.tvTag.setOnClickListener {

            DialogPicker(requireContext(), allTags, mainViewModel).show()
        }

        setupCountryPicker()

        bind.tvName.setOnClickListener {

            NameDialog(requireContext(), it as TextView, mainViewModel).show()

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

        val bundle = Bundle().apply {


            if(tag == "Tag") {
                putString("TAG", "")
            } else {
                putString("TAG", tag)
            }

            putString("COUNTRY", selectedCountry)

            if(name == "Name"){
                putString("NAME", "")
            } else {
                putString("NAME", name)
            }

            putBoolean("SEARCH_TOP", false)

        }
        mainViewModel.isNewSearch = true
        mainViewModel.setSearchBy(bundle)

        bind.rvSearchStations.smoothScrollToPosition(0)


    }


    private fun setSearchParamsObservers(){

        mainViewModel.searchParamTag.observe(viewLifecycleOwner){
            bind.tvTag.text = it
        }

        mainViewModel.searchParamName.observe(viewLifecycleOwner){
            bind.tvName.text = it
        }

        mainViewModel.searchParamCountry.observe(viewLifecycleOwner){

          selectedCountry = it
        }
    }





}





