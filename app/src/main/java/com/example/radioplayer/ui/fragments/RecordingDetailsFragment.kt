package com.example.radioplayer.ui.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.arthenica.ffmpegkit.FFmpegKit

import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.data.models.PlayingItem
import com.example.radioplayer.databinding.FragmentRecordingDetailsBinding
import com.example.radioplayer.exoPlayer.RadioService

import com.example.radioplayer.ui.dialogs.RenameRecordingDialog
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.SEARCH_FROM_RECORDINGS
import com.example.radioplayer.utils.Utils

import com.google.android.material.slider.RangeSlider
import com.google.android.material.snackbar.Snackbar

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


import java.io.File

import javax.inject.Inject


const val RECORDING_CUT_PREF = "recording cut pref"

@AndroidEntryPoint
class RecordingDetailsFragment : BaseFragment<FragmentRecordingDetailsBinding>(
    FragmentRecordingDetailsBinding::inflate
)
{
    @Inject
    lateinit var glide : RequestManager

    private var currentRecording : Recording? = null

    private var isSeekBarToUpdate = true

    private var isTrimmerWorking = false

    private var isRecordingToUpdate = false

    private var isTvTrimProcessVisible = false

    private lateinit var recordingCutterPref : SharedPreferences
    private var switchPref = true

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()

        setSeekbarChangeListener()

        setRecordingsRenameClickListener()

        setSwitchPreference()

        setRangeSeekbarListener()

        setTrimmingAudioButton()

        observeRecordingPlaylistUpdate()

        setPlaybackSpeedButtons()

        setCutExpandClickListener()
    }

    private fun setCutExpandClickListener(){

        handleCutContainerVisibility(mainViewModel.isCutExpanded)

        bind.tvCutExpander.setOnClickListener {
            mainViewModel.isCutExpanded = !mainViewModel.isCutExpanded

            handleCutContainerVisibility(mainViewModel.isCutExpanded)

        }
    }

    private fun handleCutContainerVisibility(isVisible : Boolean){

        bind.tvSwitchOption.isVisible = isVisible
        bind.switchKeepOriginal.isVisible = isVisible
        bind.rangeSlider.isVisible = isVisible
        bind.tvProcessTrim.isVisible = isVisible && isTvTrimProcessVisible

        bind.ivIcon.isVisible = !isVisible

        bind.tvCutExpander.apply {
            if(isVisible)
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_playlists_arrow_shrink, 0)
            else
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_playlists_arrow_expand, 0)
        }

    }


    private fun setPlaybackSpeedButtons(){

       updatePlaybackSpeedDisplayValue()

        bind.fabSpeedMinus.setOnClickListener {

            if(RadioService.playbackSpeed > 10){
                RadioService.playbackSpeed -= 10
                mainViewModel.updatePlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }

        }

        bind.fabSpeedPlus.setOnClickListener {
            if(RadioService.playbackSpeed < 300){
                RadioService.playbackSpeed += 10
                mainViewModel.updatePlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }
        }
    }


    private fun updatePlaybackSpeedDisplayValue(){
        bind.tvPlaybackSpeedValue.text = "${RadioService.playbackSpeed}%"
    }


    private fun setSwitchPreference(){

        recordingCutterPref = requireActivity().getSharedPreferences(RECORDING_CUT_PREF, Context.MODE_PRIVATE)

        switchPref = recordingCutterPref.getBoolean(RECORDING_CUT_PREF, true)

        bind.switchKeepOriginal.isChecked = switchPref

        bind.switchKeepOriginal.setOnCheckedChangeListener { _, isChecked ->

            switchPref = isChecked

        }

    }


    private fun setRecordingsRenameClickListener(){
        bind.tvRename.setOnClickListener {
            currentRecording?.let { recording ->
                RenameRecordingDialog(requireContext(), recording.name){ newName ->

                    mainViewModel.newPlayingItem.postValue(
                        PlayingItem.FromRecordings(
                            Recording(
                                recording.id,
                                recording.iconUri,
                                recording.timeStamp,
                                newName,
                                recording.durationMills
                            )
                        )
                    )
                    databaseViewModel.renameRecording(recording.id, newName)


                }.show()
            }
        }
    }


    private fun subscribeToObservers(){

        observeCurrentRecording()

        observePlayerPosition()

        observeRecordingDuration()


    }


    private fun observeCurrentRecording(){

        mainViewModel.newPlayingItem.observe(viewLifecycleOwner) {

        if(it is PlayingItem.FromRecordings){

            currentRecording = it.recording

            updateUiForRecording(it.recording)

            bind.seekBar.max = it.recording.durationMills.toInt()

            updateRangeSeekbar(it.recording)

            }
        }
    }


    private fun setTrimmingAudioButton(){

        bind.tvProcessTrim.setOnClickListener {
            if(!isTrimmerWorking){

                currentRecording?.let {
                    trimAudio(it)
                }
            }
        }
    }


    private fun trimAudio(rec : Recording){

        isTrimmerWorking = true

        bind.tvProcessTrim.text = "Processing..."

        val totalDuration = (rec.durationMills/1000).toInt()
        val trimStart = bind.rangeSlider.values[0].toInt()
        val trimEnd = totalDuration - bind.rangeSlider.values[1].toInt()
        val duration = (rec.durationMills/1000).toInt() - trimEnd - trimStart
        val oggFilePath = requireActivity().filesDir.absolutePath.toString() + "/" + rec.id

        val currentTime = System.currentTimeMillis()
        val output = requireActivity().filesDir.absolutePath.toString() + File.separator + currentTime + ".ogg"

        val command = arrayOf( "-ss", trimStart.toString(), "-i",
            oggFilePath, "-t", duration.toString(), "-c", "copy", output)

       FFmpegKit.executeWithArgumentsAsync(command
        ) { session ->

           var message = ""

            if(session.returnCode.isValueSuccess) {


                val newRecording = Recording(
                    id ="$currentTime.ogg",
                    iconUri = rec.iconUri,
                    timeStamp = currentTime,
                    name = rec.name,
                    durationMills = (duration*1000).toLong()
                )

                databaseViewModel.insertNewRecording(newRecording)
                message = "Success!"


                if(!switchPref){
                    databaseViewModel.deleteRecording(rec.id)
                    currentRecording = newRecording
                    isRecordingToUpdate = true

                }

            } else if(session.returnCode.isValueError){

                message = "Something went wrong!"

            }

           lifecycleScope.launch(Dispatchers.Main){
               Snackbar.make(requireActivity().findViewById(R.id.rootLayout), message, Snackbar.LENGTH_SHORT).show()
               isTrimmerWorking = false
               bind.tvProcessTrim.visibility = View.GONE
               isTvTrimProcessVisible = false
           }
          session.cancel()
        }
    }

    private fun observeRecordingPlaylistUpdate(){

        mainViewModel.isRecordingUpdated.observe(viewLifecycleOwner){
            if(isRecordingToUpdate && currentRecording != null){
                mainViewModel.playOrToggleStation(
                    rec = currentRecording,
                    searchFlag = SEARCH_FROM_RECORDINGS,
                    playWhenReady = false
                )
                isRecordingToUpdate = false
            }
        }
    }



    private fun updateRangeSeekbar(rec : Recording){

        val seconds = (rec.durationMills / 1000).toFloat()

        bind.rangeSlider.apply {

            valueTo = seconds
            setLabelFormatter { float ->

                Utils.timerFormatCut(float.toLong()*1000)
            }

            values = listOf(0f, seconds)

        }
    }

    private fun setRangeSeekbarListener(){

        bind.rangeSlider.addOnChangeListener(RangeSlider.OnChangeListener { _, _, fromUser ->
            if(fromUser && bind.tvProcessTrim.visibility == View.GONE){

                bind.tvProcessTrim.apply {
                    visibility = View.VISIBLE
                    text = "Apply"
                    isTvTrimProcessVisible = true
                }
            }
        })

    }

    private fun setSeekbarChangeListener(){
        bind.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    setTvRecordingPlayingTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
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

    private fun observeRecordingDuration(){

        mainViewModel.currentRecordingDuration.observe(viewLifecycleOwner){
            bind.seekBar.max = it.toInt()
        }


    }


    private fun observePlayerPosition(){
        mainViewModel.currentPlayerPosition.observe(viewLifecycleOwner){
            if(isSeekBarToUpdate){
                bind.seekBar.progress = it.toInt()
                setTvRecordingPlayingTime(it)
            }
        }
    }

    private fun setTvRecordingPlayingTime(time : Long){
        bind.tvRecordingPlayingTime.text = Utils.timerFormat(time)
    }


    private fun updateUiForRecording(recording: Recording){

        bind.tvName.text = recording.name
        glide
            .load(recording.iconUri)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(bind.ivIcon)

    }



    override fun onDestroyView() {
        super.onDestroyView()
        isRecordingToUpdate = false
        isTvTrimProcessVisible = false
        recordingCutterPref.edit().putBoolean(RECORDING_CUT_PREF, switchPref).apply()
        _bind = null
    }


}