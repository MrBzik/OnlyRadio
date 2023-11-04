package com.onlyradio.radioplayer.ui.fragments

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
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.adapters.RecordingsAdapter
import com.onlyradio.radioplayer.data.local.entities.Recording
import com.onlyradio.radioplayer.databinding.FragmentRecordingsBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.onlyradio.radioplayer.ui.animations.SwipeToDeleteCallback
import com.onlyradio.radioplayer.ui.animations.slideAnim
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.onlyradio.radioplayer.utils.TextViewOutlined
import com.onlyradio.radioplayer.utils.Utils
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

    companion object{
        var isToHandleNewRecording = false
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        setCurrentItemSeekbarHandler()

        subscribeToRecordings()

        setAdapterClickListener()

//        observePlayingItem()

        observeCurrentRecording()

        observePlayerPosition()

        setToolbar()

    }




    private fun setToolbar(){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            bind.viewToolbar.setBackgroundResource(R.drawable.toolbar_recordings_vector)

            (bind.tvEnableDeleting as TextViewOutlined).isSingleColor = true
            (bind.tvEnableDeleting as TextViewOutlined).setTextColor(Color.WHITE)

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
        recordingsViewModel.currentPlayerPosition.observe(viewLifecycleOwner){

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

                recordingsViewModel.currentPlayerPosition.value?.let { position ->
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
                    recordingsViewModel.seekTo(it.progress.toLong())
                    RadioService.recordingPlaybackPosition.postValue(it.progress.toLong())
                    isSeekBarToUpdate = true
                }
            }
        })
    }

    private fun observeCurrentRecording(){

        RadioService.currentPlayingRecording.observe(viewLifecycleOwner){

            currentRecording = it

//            if(RadioService.currentMediaItems == SEARCH_FROM_RECORDINGS){

            if(isToHandleNewRecording){

                    bind.rvRecordings.apply {
                        smoothScrollToPosition(mainViewModel.getPlayerCurrentIndex())
                        post {
                            val holder = findViewHolderForAdapterPosition(mainViewModel.getPlayerCurrentIndex())
                            recordingsAdapter.handleRecordingChange(it, holder as RecordingsAdapter.RecordingItemHolder)
                        }
                    }

                } else {
                    isToHandleNewRecording = true
                }
//            }
        }
    }



    private fun setAdapterClickListener(){

        recordingsAdapter.setOnClickListener { recording, position ->
            animator.cancel()
            recordingsViewModel.playOrToggleRecording(
                rec = recording,
                itemIndex = position
            )

        }
    }


    private fun subscribeToRecordings (){

        recordingsViewModel.allRecordingsLiveData.observe(viewLifecycleOwner){

            bind.tvMessage.apply {
                if(it.isEmpty()){
                    visibility = View.VISIBLE
                    slideAnim(400, 0, R.anim.fade_in_anim)

                } else{
                    visibility = View.INVISIBLE
                }
            }


            recordingsAdapter.listOfRecordings = it

            recordingsViewModel.checkRecordingsForCleanUp(it)


        }
    }




    private fun setupRecyclerView(){
        bind.rvRecordings.apply {
            adapter = recordingsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            edgeEffectFactory = BounceEdgeEffectFactory()
            setHasFixedSize(true)
            setToggleItemDeletion()

            recordingsAdapter.apply{
                alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
                titleSize = settingsViewModel.stationsTitleSize

                if(RadioService.currentMediaItems == SEARCH_FROM_RECORDINGS){
                    playingRecordingId = RadioService.currentPlayingRecording.value?.id ?: ""
                } else {
                    playingRecordingId = ""
                }
            }

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

            if(isDeletingEnabled){

                (bind.tvEnableDeleting as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.swipe_delete_on))
                itemTouchHelper.attachToRecyclerView(bind.rvRecordings)
            } else {

                (bind.tvEnableDeleting as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.swipe_delete_off))
                itemTouchHelper.attachToRecyclerView(null)
            }
        }
    }


    private val itemTouchCallback by lazy {

        object : SwipeToDeleteCallback(requireContext()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition

                val recording = recordingsAdapter.listOfRecordings[position]

                currentRecording?.let {

                    if(it.id == recording.id){
                        animator.cancel()


                        currentItemSeekbar = null
                        currentItemTvDuration = null

                        recordingsAdapter.apply {
                            playingRecordingId = ""
                            previousSeekbar = null
                            previousTvTime = null
                            previousTvTimeValue = 0
                        }

                        currentRecording = null

//                        mainViewModel.stopPlay()

                        (activity as MainActivity).bindPlayer?.root?.let { playerView ->
                            playerView.visibility = View.GONE
                            playerView.slideAnim(300, 0, R.anim.fade_out_anim)

                        }
                    }
                }

                if(RadioService.currentMediaItems == SEARCH_FROM_RECORDINGS){
                    recordingsViewModel.removeRecordingMediaItem(position)
                }


                recordingsViewModel.deleteRecording(recording.id)


                Snackbar.make(
                    requireActivity().findViewById(R.id.rootLayout),
                    resources.getString(R.string.recording_deleted),
                    Snackbar.LENGTH_LONG
                ).apply {

                    addCallback(object: BaseTransientBottomBar.BaseCallback<Snackbar>(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)

                            if(event != DISMISS_EVENT_ACTION) {

//                                val filePath = "${requireContext().filesDir}/${recording.id}"
                                recordingsViewModel.removeRecordingFile(recording.id)
                            }
                        }
                    }
                    )
                    setAction(resources.getString(R.string.action_undo)){
                        recordingsViewModel.insertNewRecording(recording)
                        if(RadioService.currentMediaItems == SEARCH_FROM_RECORDINGS){
                            recordingsViewModel.restoreRecordingMediaItem(position)
                        }
                    }
                }.show()

            }
        }

    }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(itemTouchCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentItemSeekbar = null
        currentItemTvDuration = null
        currentRecording = null
        isDeletingEnabled = false
        bind.rvRecordings.adapter = null
        _bind = null
        isToHandleNewRecording = false
    }



}