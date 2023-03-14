package com.example.radioplayer.ui.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback

import com.arthenica.ffmpegkit.ReturnCode
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.data.models.PlayingItem
import com.example.radioplayer.databinding.FragmentRecordingDetailsBinding
import com.example.radioplayer.exoPlayer.RadioService

import com.example.radioplayer.ui.dialogs.RenameRecordingDialog
import com.example.radioplayer.utils.Utils
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.google.android.material.slider.RangeSlider
import com.google.android.material.snackbar.Snackbar

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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

    private lateinit var recordingCutterPref : SharedPreferences
    private var switchPref = true

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()

        setSeekbarChangeListener()

        setRecordingsRenameClickListener()

        setSwitchPreference()
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

    }


    private fun observeCurrentRecording(){

        mainViewModel.newPlayingItem.observe(viewLifecycleOwner) {

        if(it is PlayingItem.FromRecordings){

            currentRecording = it.recording

            updateUiForRecording(it.recording)

            bind.seekBar.max = it.recording.durationMills.toInt()

            setRangeSeekbar(it.recording)

            setTrimmingAudio(it.recording)

            }
        }
    }


    private fun setTrimmingAudio(rec : Recording){

        bind.tvTrim.setOnClickListener {

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

                if(session.returnCode.isValueSuccess) {
                    Snackbar.make(requireActivity().findViewById(R.id.rootLayout), "Success!", Snackbar.LENGTH_SHORT).show()
                    databaseViewModel.insertNewRecording(
                        Recording(
                            id ="$currentTime.ogg",
                            iconUri = rec.iconUri,
                            timeStamp = currentTime,
                            name = rec.name,
                            durationMills = (duration*1000).toLong()
                        )
                    )
                } else if(session.returnCode.isValueError){
                    Snackbar.make(requireActivity().findViewById(R.id.rootLayout), "Something went wrong!", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setRangeSeekbar(rec : Recording){

        val seconds = (rec.durationMills / 1000).toFloat()

        bind.rangeSlider.apply {

            valueTo = seconds
            setLabelFormatter { float ->

                Utils.timerFormatCut(float.toLong()*1000)
            }

            values = listOf(0f, seconds)


            addOnChangeListener(RangeSlider.OnChangeListener { _, _, fromUser ->
                if(fromUser && bind.tvTrim.visibility == View.GONE){

                    bind.tvTrim.visibility = View.VISIBLE
                }
            })
        }
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
        recordingCutterPref.edit().putBoolean(RECORDING_CUT_PREF, switchPref).apply()
        _bind = null
    }


}