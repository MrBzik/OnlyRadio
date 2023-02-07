package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PagingHistoryAdapter
import com.example.radioplayer.databinding.FragmentHistoryBinding
import com.example.radioplayer.ui.MainActivity
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


    lateinit var currentDate : String


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        setupAdapterClickListener()

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
                        databaseViewModel.compareDatesWithPrefAndCLeanIfNeeded()

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

            adapter = databaseViewModel.historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        setAdapterLoadStateListener()
    }

    private fun setupAdapterClickListener(){

        databaseViewModel.historyAdapter.setOnClickListener {

            mainViewModel.playOrToggleStation(it, SEARCH_FROM_HISTORY)
            mainViewModel.newRadioStation.postValue(it)
            databaseViewModel.isStationInDB.postValue(true)

        }
    }


    private fun setAdapterLoadStateListener(){

        databaseViewModel.historyAdapter.addLoadStateListener {

            if (it.refresh is LoadState.Loading ||
                it.append is LoadState.Loading)
                bind.progressBar.isVisible = true


            else {
                bind.progressBar.visibility = View.GONE
            }
        }
    }


    override fun onDestroyView() {
        bind.rvHistory.adapter = null
        super.onDestroyView()
    }

}