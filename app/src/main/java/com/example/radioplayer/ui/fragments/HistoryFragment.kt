package com.example.radioplayer.ui.fragments

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PagingHistoryAdapter
import com.example.radioplayer.databinding.FragmentHistoryBinding
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.example.radioplayer.ui.dialogs.HistorySettingsDialog
import com.example.radioplayer.ui.dialogs.HistoryWarningDialog
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Utils.fromDateToString
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.sql.Date
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(
    FragmentHistoryBinding::inflate
) {


    private var currentDate = ""

    @Inject
    lateinit var glide : RequestManager

    @Inject
    lateinit var historyAdapter: PagingHistoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateCurrentDate()

        setupRecyclerView()

        setupAdapterClickListener()

        subscribeToHistory()

        showSelectedHistoryOption()

        historySettingsClickListener()

        setOnSaveOptionsClickListener()


    }

    private fun showSelectedHistoryOption(){

       val option = databaseViewModel.getHistoryOptionsPref()

        bind.tvCurrentMode.text = option
    }


    private fun setOnSaveOptionsClickListener(){

        bind.tvSaveOption.setOnClickListener {

            val previousOption = databaseViewModel.getHistoryOptionsPref()
            val newOption = bind.tvCurrentMode.text.toString()
            val prevVal = databaseViewModel.getDatesValueOfPref(previousOption)
            val newVal = databaseViewModel.getDatesValueOfPref(newOption)

             if (prevVal <= newVal) {

                 saveNewOption(newOption)

             } else {
                HistoryWarningDialog(requireContext()){
                    saveNewOption(newOption)
                        databaseViewModel.compareDatesWithPrefAndCLeanIfNeeded(null)

                }.show()
             }
        }
    }

    private fun saveNewOption(newOption : String){

        databaseViewModel.setHistoryOptionsPref(newOption)
        bind.tvSaveOption.isVisible = false
        Snackbar.make(
            (activity as MainActivity).findViewById(R.id.rootLayout),
            "Option was saved",
            Snackbar.LENGTH_SHORT).show()

    }

    private fun historySettingsClickListener(){

        bind.tvHistorySettings.setOnClickListener {

            HistorySettingsDialog(requireContext()){ newOption ->

                bind.tvCurrentMode.text = newOption

                bind.tvSaveOption.isVisible = newOption != databaseViewModel.getHistoryOptionsPref()

            }.show()

        }
    }



    private fun setupRecyclerView (){


        bind.rvHistory.apply {

            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
            edgeEffectFactory = BounceEdgeEffectFactory()
            setHasFixedSize(true)

            mainViewModel.newRadioStation.value?.let {

            historyAdapter.currentRadioStationID = it.stationuuid

            }


        }

        setAdapterLoadStateListener()
    }

    private fun subscribeToHistory(){

        viewLifecycleOwner.lifecycleScope.launch{

            databaseViewModel.historyFlow.collectLatest {

                historyAdapter.currentDate = currentDate
                historyAdapter.submitData(it)

            }
        }

    }

    private fun setupAdapterClickListener(){

        historyAdapter.setOnClickListener {

            mainViewModel.playOrToggleStation(it, SEARCH_FROM_HISTORY)

        }
    }



    private fun setAdapterLoadStateListener(){

        historyAdapter.addLoadStateListener {

            if (it.refresh is LoadState.Loading ||
                it.append is LoadState.Loading)
                bind.progressBar.isVisible = true


            else {
                bind.progressBar.visibility = View.GONE
            }
        }
    }


    private fun updateCurrentDate(){

        val time = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.time = Date(time)
        val parsedDate = fromDateToString(calendar)
        currentDate =  parsedDate

    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind.rvHistory.adapter = null
        _bind = null
    }

}