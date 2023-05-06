package com.example.radioplayer.ui.fragments


import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

//import com.arthenica.ffmpegkit.FFmpegKit

import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.data.models.PlayingItem
import com.example.radioplayer.databinding.FragmentRecordingDetailsBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.MainActivity

import com.example.radioplayer.ui.dialogs.RenameRecordingDialog
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

        setTrimmingProcesserButton()

        observeRecordingPlaylistUpdate()

        setPlaybackSpeedButtons()

        setCutExpandClickListener()
    }

    private fun setCutExpandClickListener(){

        handleCutContainerVisibility(mainViewModel.isCutExpanded)

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            bind.btnCutExpander?.setOnClickListener {
                mainViewModel.isCutExpanded = !mainViewModel.isCutExpanded

                handleCutContainerVisibility(mainViewModel.isCutExpanded)
            }
        } else {
            bind.tvCutExpander.setOnClickListener {
                mainViewModel.isCutExpanded = !mainViewModel.isCutExpanded

                handleCutContainerVisibility(mainViewModel.isCutExpanded)
            }
        }
    }

    private fun handleCutContainerVisibility(isVisible : Boolean){

        bind.tvSwitchOption.isVisible = isVisible
        bind.switchKeepOriginal.isVisible = isVisible
        bind.rangeSlider.isVisible = isVisible
        bind.tvProcessTrim.isVisible = isVisible && isTvTrimProcessVisible

        if(isVisible) bind.ivIcon.visibility = View.GONE
            else bind.ivIcon.visibility = View.VISIBLE


        bind.tvCutExpander.apply {
            if(isVisible)
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_shrink_rec, 0)
            else
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_expand_rec, 0)
        }

    }


    private fun setPlaybackSpeedButtons(){

       updatePlaybackSpeedDisplayValue()

        bind.fabSpeedMinus.setOnClickListener {

            if(RadioService.playbackSpeedRec > 10){
                RadioService.playbackSpeedRec -= 10
                mainViewModel.updateRecPlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }

        }

        bind.fabSpeedPlus.setOnClickListener {
            if(RadioService.playbackSpeedRec < 400){
                RadioService.playbackSpeedRec += 10
                mainViewModel.updateRecPlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }
        }
    }


    private fun updatePlaybackSpeedDisplayValue(){
        bind.tvPlaybackSpeedValue.text = "${RadioService.playbackSpeedRec}%"
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
                RadioService.currentPlayingRecording.postValue(
                            Recording(
                                recording.id,
                                recording.iconUri,
                                recording.timeStamp,
                                newName,
                                recording.durationMills
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

        RadioService.currentPlayingRecording.observe(viewLifecycleOwner) {

            currentRecording = it

            updateUiForRecording(it)

            bind.seekBar.max = it.durationMills.toInt()

            updateRangeSeekbar(it)

        }
    }


    private fun setTrimmingProcesserButton(){

        bind.tvProcessTrim.setOnClickListener {
            if(!isTrimmerWorking){

                currentRecording?.let {
                    trimAudio(it)
                }
            }
        }
    }




    private fun observeRecordingPlaylistUpdate(){

        mainViewModel.isRecordingUpdated.observe(viewLifecycleOwner){
            if(isRecordingToUpdate && currentRecording != null){
                mainViewModel.playOrToggleRecording(
                    rec = currentRecording!!,
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

        if(recording.iconUri.isBlank()){
            bind.ivIcon.visibility = View.GONE
        } else {

            glide
                .load(recording.iconUri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(bind.ivIcon)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isRecordingToUpdate = false
        isTvTrimProcessVisible = false
        recordingCutterPref.edit().putBoolean(RECORDING_CUT_PREF, switchPref).apply()
        _bind = null
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

//       FFmpegKit.executeWithArgumentsAsync(command
//        ) { session ->
//
//           var message = ""
//
//            if(session.returnCode.isValueSuccess) {
//
//
//                val newRecording = Recording(
//                    id ="$currentTime.ogg",
//                    iconUri = rec.iconUri,
//                    timeStamp = currentTime,
//                    name = rec.name,
//                    durationMills = (duration*1000).toLong()
//                )
//
//                databaseViewModel.insertNewRecording(newRecording)
//                message = "Success!"
//
//
//                if(!switchPref){
//                    databaseViewModel.deleteRecording(rec.id)
//                    currentRecording = newRecording
//                    isRecordingToUpdate = true
//
//                }
//
//            } else if(session.returnCode.isValueError){
//
//                message = "Something went wrong!"
//
//            }
//
//           lifecycleScope.launch(Dispatchers.Main){
//               Snackbar.make(requireActivity().findViewById(R.id.rootLayout), message, Snackbar.LENGTH_SHORT).show()
//               isTrimmerWorking = false
//               bind.tvProcessTrim.visibility = View.GONE
//               isTvTrimProcessVisible = false
//           }
//          session.cancel()
//        }
    }


}