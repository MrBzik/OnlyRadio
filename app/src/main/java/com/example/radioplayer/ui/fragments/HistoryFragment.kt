package com.example.radioplayer.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.PagingHistoryAdapter
import com.example.radioplayer.adapters.RadioDatabaseAdapter
import com.example.radioplayer.databinding.FragmentHistoryBinding
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.dialogs.HistorySettingsDialog
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.utils.Constants.DATE_FORMAT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    lateinit var bind : FragmentHistoryBinding

    lateinit var databaseViewModel: DatabaseViewModel

    @Inject
    lateinit var historyAdapter : PagingHistoryAdapter

    lateinit var currentDate : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        bind = FragmentHistoryBinding.inflate(inflater, container, false)

        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseViewModel = (activity as MainActivity).databaseViewModel

        updateCurrentDate()

        setupRecyclerView()

        subscribeToHistory()

        historySettingsClickListener()

        setOnSaveOptionsClickListener()

    }

    private fun setOnSaveOptionsClickListener(){

        bind.tvSaveOption.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Saving new option may result in cleaning history")
                .show()

        }


    }

    private fun historySettingsClickListener(){

        bind.tvHistorySettings.setOnClickListener {

            HistorySettingsDialog(requireContext()){

                bind.tvCurrentMode.text = it
                bind.tvSaveOption.isVisible = true

            }.show()

        }
    }

    private fun subscribeToHistory(){

        lifecycleScope.launch{

            databaseViewModel.getStationsHistory().collectLatest {

                historyAdapter.currentDate = currentDate
                historyAdapter.submitData(it)

            }
        }

    }

    private fun updateCurrentDate(){

       val time = System.currentTimeMillis()
       val format = SimpleDateFormat(DATE_FORMAT)
       currentDate =  format.format(time)

    }

    private fun setupRecyclerView (){

        bind.rvHistory.apply {

            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        setAdapterLoadStateListener()
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


}