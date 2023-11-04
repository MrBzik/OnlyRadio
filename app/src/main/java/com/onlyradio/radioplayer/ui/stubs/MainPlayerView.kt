package com.onlyradio.radioplayer.ui.stubs

import android.graphics.drawable.AnimatedVectorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.databinding.StubPlayerActivityMainBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.extensions.loadSingleImage
import com.onlyradio.radioplayer.ui.animations.slideAnim
import com.onlyradio.radioplayer.utils.Constants
import com.onlyradio.radioplayer.utils.RandomColors
import com.onlyradio.radioplayer.utils.Utils


class MainPlayerView(
    private val bind: StubPlayerActivityMainBinding,
    private val glide: RequestManager,
    private val timeLeftPrefix: String
)  {


    fun slideInPlayer(){
        bind.root.visibility = View.VISIBLE
        bind.root.slideAnim(500, 0, R.anim.fade_in_anim)
        bind.tvStationTitle.isSingleLine = true
        bind.tvStationTitle.isSelected = true
    }




    fun handleIcons (playbackState: Boolean){

        val colorId = if (playbackState)
            R.color.selected_text_color
        else R.color.regular_text_color
        val drawableId = if (playbackState)
            R.drawable.play_state_animate_to_playing
        else R.drawable.play_state_animate_to_pause

        val drawable = ContextCompat.getDrawable(bind.root.context, drawableId) as AnimatedVectorDrawable
        bind.ivTogglePlayCurrentStation.setImageDrawable(drawable)
        drawable.start()

        bind.tvStationTitle.setTextColor(ContextCompat.getColor(bind.root.context, colorId))
    }



    fun handleTitleText(title : String){

        if(title.isBlank()){
            bind.tvStationTitle.apply {
                text = resources.getString(R.string.playing_no_info)
                setTextColor(ContextCompat.getColor(bind.root.context,R.color.regular_text_color))
            }
        } else {
            bind.tvStationTitle.apply {
                text = title
                setTextColor(ContextCompat.getColor(bind.root.context,R.color.selected_text_color))

            }
        }
    }


    fun updateRecordingDuration(dur : Long){

        if(RadioService.isFromRecording)
            bind.tvBitrate.text = "$timeLeftPrefix " + Utils.timerFormatCut(dur)
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
            glide.loadSingleImage(
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
}


