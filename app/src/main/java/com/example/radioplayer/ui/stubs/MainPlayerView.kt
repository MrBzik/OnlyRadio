package com.example.radioplayer.ui.stubs

import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.databinding.FragmentPlayerBinding
import com.example.radioplayer.databinding.StubPlayerActivityMainBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.extensions.loadImage
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.ui.viewmodels.RecordingsViewModel
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.RandomColors
import javax.inject.Inject


class MainPlayerView(private val bind : StubPlayerActivityMainBinding,
                     private val glide : RequestManager
                     )  {


    fun slideInPlayer(){
        bind.root.visibility = View.VISIBLE
        bind.root.slideAnim(500, 0, R.anim.fade_in_anim)
        bind.tvStationTitle.isSingleLine = true
        bind.tvStationTitle.isSelected = true
    }




     fun handleIcons (playbackState: PlaybackStateCompat?){

        playbackState?.let {
            when{
                it.isPlaying -> {
                    bind.ivTogglePlayCurrentStation
                        .setImageResource(R.drawable.ic_pause_play)
                    bind.tvStationTitle.apply {
                        setTextColor(ContextCompat.getColor(bind.root.context,R.color.selected_text_color))
                    }
                }
                it.isPlayEnabled -> {
                    bind.ivTogglePlayCurrentStation
                        .setImageResource(R.drawable.ic_play_pause)
                    bind.tvStationTitle.apply {
                        setTextColor(ContextCompat.getColor(bind.root.context, R.color.regular_text_color))
                    }
                }

                else -> Unit
            }
        }
    }



    fun handleTitleText(title : String){

        if(title.equals("NULL", ignoreCase = true) || title.isBlank()){
            bind.tvStationTitle.apply {
                text = Constants.TITLE_UNKNOWN
                setTextColor(ContextCompat.getColor(bind.root.context,R.color.regular_text_color))
            }
        } else {

            bind.tvStationTitle.apply {
                text = title
                setTextColor(ContextCompat.getColor(bind.root.context,R.color.selected_text_color))

            }
        }
    }


     fun updateImage(){

        var newName = ""
        var isRecording = false
        var newImage = ""

        if (RadioService.currentMediaItems != Constants.SEARCH_FROM_RECORDINGS) {

            RadioService.currentPlayingStation.value?.apply {
                newName = name ?: "X"
                newImage = favicon ?: ""
                val bits = if(bitrate == 0) "0? kbps" else "$bitrate kbps"
                bind.tvBitrate.text = bits
            }

        } else  {

            RadioService.currentPlayingRecording.value?.apply {
                isRecording = true
                bind.tvStationTitle.text = name
                bind.tvBitrate.text = ""
                newName = name
                newImage = iconUri
            }
        }

        if(newImage.isBlank()){
            bind.ivCurrentStationImage.visibility = View.GONE
            setTvPlaceHolderLetter(newName, isRecording)

        } else {

            bind.ivCurrentStationImage.visibility = View.VISIBLE
            glide.loadImage(
                uri = newImage,
                tvPlaceholder = bind.tvPlaceholder,
                ivItemImage = bind.ivCurrentStationImage,
            ){
                setTvPlaceHolderLetter(newName, isRecording)
            }
        }
    }


    private val randColors = RandomColors()

    private fun setTvPlaceHolderLetter(name : String, isRecording : Boolean){

        val color = randColors.getColor()

        if(isRecording){

            bind.tvPlaceholder.apply {
                text = "Rec."
                setTextColor(color)
                alpha = 0.6f
                textSize = 28f
            }
        } else {

            var char = 'X'

            for(l in name.indices){
                if(name[l].isLetter()){
                    char = name[l]
                    break
                }
            }

            bind.tvPlaceholder.apply {
                text = char.toString().uppercase()
                setTextColor(color)
                alpha = 0.6f
                textSize = 40f
            }
        }
    }



//    override fun onDestroyView() {
//        super.onDestroyView()
//        _bind = null
//    }

}


