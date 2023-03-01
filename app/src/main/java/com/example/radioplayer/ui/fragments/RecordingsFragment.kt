package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.radioplayer.R
import com.example.radioplayer.adapters.RecordingsAdapter
import com.example.radioplayer.databinding.FragmentRecordingsBinding
import com.example.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecordingsFragment : BaseFragment<FragmentRecordingsBinding>(
    FragmentRecordingsBinding::inflate
) {

    @Inject
    lateinit var recordingsAdapter : RecordingsAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        subscribeToRecordings()

        setAdapterClickListener()
    }

    private fun setAdapterClickListener(){

        recordingsAdapter.setOnClickListener {
            mainViewModel.playOrToggleStation(rec = it, searchFlag = SEARCH_FROM_RECORDINGS)
        }
    }


    private fun subscribeToRecordings (){

        databaseViewModel.allRecordingsLiveData.observe(viewLifecycleOwner){

            recordingsAdapter.listOfRecordings = it

        }
    }

    private fun setupRecyclerView(){
        bind.rvRecordings.apply {
            adapter = recordingsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            edgeEffectFactory = BounceEdgeEffectFactory()
            setHasFixedSize(true)
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind.rvRecordings.adapter = null
        _bind = null
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
            val recording = recordingsAdapter.listOfRecordings[position]


            databaseViewModel.deleteRecording(recording)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootLayout),
                "Recording was deleted",
                Snackbar.LENGTH_LONG
                ).apply {

                    addCallback(object: BaseTransientBottomBar.BaseCallback<Snackbar>(){
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)

            if(event == DISMISS_EVENT_CONSECUTIVE ||
               event == DISMISS_EVENT_TIMEOUT ||
               event == DISMISS_EVENT_SWIPE ) {
                try {
                    requireActivity().deleteFile(recording.id)
                } catch (e: java.lang.Exception) {
                    Log.d("CHECKTAGS", e.stackTraceToString())
                }
            }
        }
    })

                setAction("UNDO"){
                    databaseViewModel.insertNewRecording(recording)
                }
            }.show()

        }
    }
}