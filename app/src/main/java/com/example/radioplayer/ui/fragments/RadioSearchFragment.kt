package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PagingRadioAdapter
import com.example.radioplayer.adapters.RadioAdapter
import com.example.radioplayer.databinding.FragmentRadioSearchBinding
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.Status
import com.hbb20.countrypicker.models.CPCountry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RadioSearchFragment : Fragment() {

    lateinit var bind : FragmentRadioSearchBinding

    lateinit var viewModel : MainViewModel

    lateinit var toggle : ActionBarDrawerToggle

    private var selectedCountry = ""



//    @Inject
//    lateinit var radioAdapter : RadioAdapter

    @Inject
    lateinit var pagingRadioAdapter : PagingRadioAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

        activity?.title = "Search your radio stations"

        toggle = ActionBarDrawerToggle((activity as MainActivity), bind.drawerLayout, R.string.open, R.string.close )
        bind.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setSearchDrawer()


        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        setRecycleView()

//        subscribeToObservers()

        pagingRadioAdapter.setOnClickListener {

            viewModel.playOrToggleStation(it, true)
        }
    }

//    private fun subscribeToObservers(){
//
//        viewModel.mediaItems.observe(viewLifecycleOwner){
//
//            when(it.status){
//                Status.SUCCESS -> {
//                   bind.allSongsProgressBar.isVisible = false
//                    it.data?.let { stations ->
//                        radioAdapter.listOfStations = stations
//                    }
//                }
//                Status.ERROR -> Unit
//
//                Status.LOADING -> bind.allSongsProgressBar.isVisible = true
//
//            }
//        }
//    }



    private fun setRecycleView(){

        bind.rvSearchStations.apply {

//            adapter = radioAdapter

            adapter = pagingRadioAdapter

            layoutManager = LinearLayoutManager(requireContext())
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return toggle.onOptionsItemSelected(item)


    }


    private fun setSearchDrawer(){

        bind.btnAcceptTag.setOnClickListener {
            bind.etTag.text.apply {
                if(this.isEmpty()) {
                    bind.tvChosenTag.text = "none"
                } else {
                    bind.tvChosenTag.text = this
                }
            }
        }

        bind.btnAcceptName.setOnClickListener {
            bind.etName.text.apply {
                if(this.isEmpty()) {
                    bind.tvChosenName.text = "none"
                } else {
                    bind.tvChosenName.text = this
                }
            }
        }

        bind.countryPicker.cpViewHelper.cpViewConfig.viewTextGenerator = { cpCountry: CPCountry ->
            "${cpCountry.name} (${cpCountry.alpha2})"
        }
        bind.countryPicker.cpViewHelper.refreshView()

        bind.countryPicker.cpViewHelper.selectedCountry.observe(viewLifecycleOwner){ code ->

            selectedCountry = code?.alpha2 ?: ""

        }


        bind.btnSearch.setOnClickListener {
           val stations = viewModel.searchStationsPaging(
                tag = bind.tvChosenTag.text.toString(),
                country = selectedCountry,
                name = bind.tvChosenName.text.toString()
            )

            lifecycleScope.launch {

                repeatOnLifecycle(Lifecycle.State.STARTED) {

                    stations.collectLatest {
                        pagingRadioAdapter.submitData(it)
                    }
                }
            }




        }

    }




}