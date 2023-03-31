package com.example.radioplayer.ui.fragments

import android.animation.ValueAnimator
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.radioplayer.R
import com.example.radioplayer.adapters.RecordingsAdapter
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.data.models.PlayingItem
import com.example.radioplayer.databinding.FragmentRecordingsBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.dialogs.RecordingSettingsDialog
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.example.radioplayer.utils.TextViewOutlined
import com.example.radioplayer.utils.Utils
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

    private var currentRecording : Recording? = null

    private var currentItemSeekbar : SeekBar? = null
    private var currentItemTvDuration : TextView? = null

    private var isSeekBarToUpdate = true

    private var isInitialLaunchOrNewItem = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        setCurrentItemSeekbarHandler()

        subscribeToRecordings()

        setAdapterClickListener()

        observePlayingItem()

        observePlayerPosition()

        setToolbar()

    }

    private fun setToolbar(){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            bind.viewToolbar.setBackgroundResource(R.drawable.toolbar_recordings_vector)

            val color = ContextCompat.getColor(requireContext(), R.color.nav_bar_rec_frag)
            val colorStatus = ContextCompat.getColor(requireContext(), R.color.status_bar_rec_frag)

            (activity as MainActivity).apply {
                window.navigationBarColor = color
                window.statusBarColor = colorStatus
            }

//            (bind.tvEnableDeleting as TextViewOutlined).isSingleColor = true


        } else {
            bind.viewToolbar.setBackgroundColor(Color.BLACK)
        }
    }



    val animator = ValueAnimator().apply {
        duration = 500
        interpolator = LinearInterpolator()
        addUpdateListener { animation ->
            currentItemSeekbar?.progress = animation.animatedValue as Int
        }
    }

    private fun updateSeekBar(progress: Int) {

        animator.setIntValues(currentItemSeekbar?.progress ?: 0, progress)

        animator.start()
    }


    private fun observePlayerPosition(){
        mainViewModel.currentPlayerPosition.observe(viewLifecycleOwner){

            if(isSeekBarToUpdate){

                setTvRecordingPlayingTime(it)

                if(isInitialLaunchOrNewItem){

                    animator.cancel()
                    currentItemSeekbar?.progress = it.toInt()
                    isInitialLaunchOrNewItem = false
                } else {
                    animator.cancel()
                    updateSeekBar(it.toInt())
                }
            }
        }
    }




    private fun setCurrentItemSeekbarHandler(){
        recordingsAdapter.setItemSeekbarHandler { seekbar, tvDuration, isItemChanged ->
            animator.cancel()
            isInitialLaunchOrNewItem = true
            seekbar.visibility = View.VISIBLE
            currentItemSeekbar = seekbar
            currentItemTvDuration = tvDuration

            if(!isItemChanged){

                mainViewModel.currentPlayerPosition.value?.let { position ->
                    seekbar.progress = position.toInt()
                    setTvRecordingPlayingTime(position)
                }
            }
            setSeekbarChangeListener(seekbar)
        }
    }


    private fun setTvRecordingPlayingTime(time : Long){
        currentItemTvDuration?.text = Utils.timerFormat(time)
    }

    private fun setSeekbarChangeListener(seekBar: SeekBar){
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    setTvRecordingPlayingTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                animator.cancel()
                isSeekBarToUpdate = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    RadioService.recordingPlaybackPosition.postValue(it.progress.toLong())
                    isSeekBarToUpdate = true
                }
            }
        })
    }



    private fun observePlayingItem(){

        mainViewModel.newPlayingItem.observe(viewLifecycleOwner){

            if(it is PlayingItem.FromRecordings){

                currentRecording = it.recording

                recordingsAdapter.playingRecordingId = it.recording.id

            } else {
                recordingsAdapter.playingRecordingId = "null"
            }
        }
    }


    private fun setAdapterClickListener(){

        recordingsAdapter.setOnClickListener { recording ->
            animator.cancel()
            mainViewModel.playOrToggleStation(
                rec = recording,
                searchFlag = SEARCH_FROM_RECORDINGS
            )

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
            setToggleItemDeletion()

            recordingsAdapter.alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10

            layoutAnimation = (activity as MainActivity).layoutAnimationController

            post {
                scheduleLayoutAnimation()
            }

        }
    }

    private var isDeletingEnabled = false

    private fun setToggleItemDeletion(){

        bind.tvEnableDeleting.setOnClickListener {

            isDeletingEnabled = !isDeletingEnabled

            if(!isDeletingEnabled){

                bind.tvEnableDeleting.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_changed_on_interaction))
                itemTouchHelper.attachToRecyclerView(bind.rvRecordings)
            } else {

                bind.tvEnableDeleting.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_button_rec))
                itemTouchHelper.attachToRecyclerView(null)
            }
        }
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

            currentRecording?.let {

                if(it.id == recording.id){
                    animator.cancel()

                    currentItemSeekbar = null
                    currentItemTvDuration = null

                        recordingsAdapter.apply {
                            playingRecordingId = "null"
                            previousSeekbar = null
                            previousTvTime = null
                            previousTvTimeValue = 0
                        }

                    currentRecording = null
                    mainViewModel.stopPlay()

                    (activity as MainActivity).bindPlayer.root.apply {
                        visibility = View.GONE
                        slideAnim(300, 0, R.anim.fade_out_anim)

                        }
                    }
                }


            databaseViewModel.deleteRecording(recording.id)


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

                databaseViewModel.removeRecordingFile(recording.id)

                }
            }
        }
    )
                setAction("UNDO"){
                    databaseViewModel.insertNewRecording(recording)
                }
            }.show()

        }
    }

    private val itemTouchHelper = ItemTouchHelper(itemTouchCallback)

    override fun onDestroyView() {
        super.onDestroyView()
        currentItemSeekbar = null
        currentItemTvDuration = null
        currentRecording = null
        isDeletingEnabled = false
        bind.rvRecordings.adapter = null
        _bind = null
    }


}