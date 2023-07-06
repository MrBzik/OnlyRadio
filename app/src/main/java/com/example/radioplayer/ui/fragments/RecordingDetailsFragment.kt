package com.example.radioplayer.ui.fragments


//import com.arthenica.ffmpegkit.FFmpegKit


import android.content.res.Configuration
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.databinding.FragmentRecordingDetailsBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.dialogs.RenameRecordingDialog
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class RecordingDetailsFragment : BaseFragment<FragmentRecordingDetailsBinding>(
    FragmentRecordingDetailsBinding::inflate
)
{
    @Inject
    lateinit var glide : RequestManager

    private var currentRecording : Recording? = null

    private var isSeekBarToUpdate = true

//    private var isRecordingToUpdate = false

    private val calendar = Calendar.getInstance()


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()

        setSeekbarChangeListener()

        setRecordingsRenameClickListener()

//        observeRecordingPlaylistUpdate()

        setPlaybackSpeedButtons()

//        setSystemBarsColor()



    }


//    private fun setSystemBarsColor(){
//
//        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
//
//            val color = when(mainViewModel.currentFragment){
//
//                Constants.FRAG_SEARCH -> ContextCompat.getColor(requireContext(), R.color.nav_bar_search_fragment)
//                Constants.FRAG_FAV -> ContextCompat.getColor(requireContext(), R.color.nav_bar_fav_fragment)
//                Constants.FRAG_HISTORY -> ContextCompat.getColor(requireContext(), R.color.nav_bar_history_frag)
//                Constants.FRAG_REC -> ContextCompat.getColor(requireContext(), R.color.nav_bar_rec_frag)
//                else -> ContextCompat.getColor(requireContext(), R.color.nav_bar_settings_frag)
//            }
//
//            (activity as MainActivity).apply {
//                window.navigationBarColor = color
//                window.statusBarColor = color
//            }
//        }
//    }



    private fun setPlaybackSpeedButtons(){

       updatePlaybackSpeedDisplayValue()

        bind.fabSpeedMinus.setOnClickListener {

            if(RadioService.playbackSpeedRec > 10){
                RadioService.playbackSpeedRec -= 10
                recordingsViewModel.updateRecPlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }

        }

        bind.fabSpeedPlus.setOnClickListener {
            if(RadioService.playbackSpeedRec < 400){
                RadioService.playbackSpeedRec += 10
                recordingsViewModel.updateRecPlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }
        }
    }


    private fun updatePlaybackSpeedDisplayValue(){
        bind.tvPlaybackSpeedValue.text = "${RadioService.playbackSpeedRec}%"
    }




    private fun renameLogic(){
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


                recordingsViewModel.renameRecording(recording.id, newName)


            }.show()
        }
    }

    private fun setRecordingsRenameClickListener(){
        bind.tvRename?.setOnClickListener {
            renameLogic()
        } ?: kotlin.run {
            bind.btnRename?.setOnClickListener {
                renameLogic()
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

            lifecycleScope.launch(Dispatchers.IO){
                val filePath = requireContext().filesDir.path + "/" + it.id
                val file = File(filePath)
                val length = Formatter.formatFileSize(requireContext(), file.length())
                withContext(Dispatchers.Main){
                    bind.tvOccupiedSpace.text = length
                }

            }

            calendar.time = Date(it.timeStamp)

           bind.tvDate.text = Utils.fromDateToStringShortWithTime(calendar)

//            getDecibels()

        }
    }



//    private fun getDecibels(){
//       currentRecording?.let {
//
//           val path = "${requireContext().filesDir}/${it.id.replace("ogg", "wav")}"
//
//
//           val file = File(path)
//
//
//          val byteArray = file.readBytes()
//
//           val calc = AudioCalculator()
//
//           calc.setBytes(byteArray)
//
//           val ampl = calc.getAmplitude(byteArray)
//           val ampls = calc.getAmplitudes(byteArray)
//           val decs = calc.getDecibels()

//           val dec = calc.getDecibel()
//
//           Log.d("CHECKTAGS", "decibel? : $dec")
//           Log.d("CHECKTAGS", "ampl? : $ampl")

//           ampls.forEach {
//
//               Log.d("CHECKTAGS", "ampls? : $it")
//           }
//
//           decs.forEach {
//
//               Log.d("CHECKTAGS", "decs? : $it")
//           }

//
//       }
//    }




//    private fun observeRecordingPlaylistUpdate(){
//
//        mainViewModel.isRecordingUpdated.observe(viewLifecycleOwner){
//            if(isRecordingToUpdate && currentRecording != null){
//                mainViewModel.playOrToggleRecording(
//                    rec = currentRecording!!,
//                    playWhenReady = false
//                )
//                isRecordingToUpdate = false
//            }
//        }
//    }






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
                    recordingsViewModel.seekTo(it.progress.toLong())
                    RadioService.recordingPlaybackPosition.postValue(it.progress.toLong())
                    isSeekBarToUpdate = true
                }
            }
        })
    }

    private fun observeRecordingDuration(){

        recordingsViewModel.currentRecordingDuration.observe(viewLifecycleOwner){
            bind.seekBar.max = it.toInt()
        }


    }


    private fun observePlayerPosition(){
        recordingsViewModel.currentPlayerPosition.observe(viewLifecycleOwner){
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
//        isRecordingToUpdate = false
        _bind = null
    }




}