package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.RadioDatabaseAdapter
import com.example.radioplayer.databinding.FragmentHistoryBinding
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    lateinit var bind : FragmentHistoryBinding

    lateinit var databaseViewModel: DatabaseViewModel

    @Inject
    lateinit var historyAdapter : RadioDatabaseAdapter

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

        setupRecyclerView()



    }


    private fun setupRecyclerView (){

        bind.rvHistory.apply {

            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }



    }


}