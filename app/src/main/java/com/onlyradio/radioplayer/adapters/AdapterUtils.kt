package com.onlyradio.radioplayer.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.databinding.ItemRadioWithTextBinding
import com.onlyradio.radioplayer.extensions.loadImage
import com.onlyradio.radioplayer.utils.Logger
import com.onlyradio.radioplayer.utils.RandomColors

interface AdapterUtils {

    var currentPlaybackState : Boolean

    fun initialiseValues(context : Context, fontSize : Float)

    fun restoreState(bind: ItemRadioWithTextBinding)

    fun handleStationPlaybackState(bind: ItemRadioWithTextBinding)

    fun onPlaybackState(isPlaying: Boolean) : Boolean

    fun handleBinding(
        bind: ItemRadioWithTextBinding,
        station: RadioStation,
        position : Int,
        checkPosition : () -> Int,
        glide : RequestManager
    )

    var onItemClickListener : ((RadioStation, Int) -> Unit)?
    var historyItemClickListener : ((RadioStation, Int, Boolean) -> Unit)?
    fun setOnClickListener(listener : (RadioStation, Int) -> Unit)

    fun setOnClickListener(listener : (RadioStation, Int, Boolean) -> Unit)


}

class AdapterUtilsImpl : AdapterUtils {

    private val randColors = RandomColors()

    override var currentPlaybackState = false
    private var defaultTextColor = 0
    private var selectedTextColor = 0
    private var defaultSecondaryTextColor = 0
    private var selectedSecondaryTextColor = 0

    var alpha = 0.1f
    private var titleSize = 18f

    private val defaultBG = R.drawable.item_radio_bg_default
    private val playingBG = R.drawable.item_radio_bg_playing
    private val pausedBG = R.drawable.item_radio_bg_paused


    override fun onPlaybackState(isPlaying: Boolean) : Boolean {
        if(currentPlaybackState != isPlaying){
            currentPlaybackState = isPlaying
            return true
        }
        return false
    }


    override fun initialiseValues(context : Context, fontSize : Float){
        defaultTextColor = ContextCompat.getColor(context, R.color.default_text_color)
        selectedTextColor = ContextCompat.getColor(context, R.color.selected_text_color)

        defaultSecondaryTextColor = ContextCompat.getColor(context, R.color.default_secondary_text_color)
        selectedSecondaryTextColor = ContextCompat.getColor(context, R.color.selected_secondary_text_color)
        alpha = context.resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
        titleSize = fontSize
    }

    override fun restoreState(bind: ItemRadioWithTextBinding){
        bind.apply {
            radioItemRootLayout.setBackgroundResource(defaultBG)
            tvPrimary.setTextColor(defaultTextColor)

            tvSecondary.setTextColor(defaultSecondaryTextColor)

        }
    }

    override fun handleStationPlaybackState(bind: ItemRadioWithTextBinding){
        if(currentPlaybackState){
            bind.apply {
                radioItemRootLayout.setBackgroundResource(playingBG)
                tvPrimary.setTextColor(selectedTextColor)

                tvSecondary.setTextColor(selectedSecondaryTextColor)

            }

        } else {
            bind.apply {
                radioItemRootLayout.setBackgroundResource(pausedBG)
                tvPrimary.setTextColor(defaultTextColor)

                tvSecondary.setTextColor(defaultSecondaryTextColor)
            }
        }
    }

    override fun handleBinding(
        bind: ItemRadioWithTextBinding,
        station: RadioStation,
        position : Int,
        checkPosition : () -> Int,
        glide : RequestManager
    ){

        bind.apply {

            tvPrimary.text = station.name
            tvPrimary.textSize = titleSize

            tvSecondary.apply {
                if(station.country.isNullOrBlank() && station.state.isNullOrBlank())
                    visibility = View.GONE

                else {
                    visibility = View.VISIBLE

                    val state = if(station.state?.isBlank() == true) ""
                    else "${station.state}, "
                    text = "$state${station.country}"

                }
            }


            station.name?.let { name ->
                var char = 'X'

                for(l in name.indices){
                    if(name[l].isLetter()){
                        char = name[l]
                        break
                    }
                }

                val color = randColors.getColor()

                tvPlaceholder.text = char.toString().uppercase()
                tvPlaceholder.setBackgroundColor(color)
                tvPlaceholder.alpha = alpha
            }


            if(station.favicon.isNullOrBlank()) {

                ivItemImage.visibility = View.GONE

            } else {

                glide.loadImage(
                    uri = station.favicon,
                    tvPlaceholder = tvPlaceholder,
                    ivItemImage = ivItemImage,
                    alpha = alpha,
                    position = position
                ){
                    checkPosition()
                }
            }
        }
    }

    override var onItemClickListener : ((RadioStation, Int) -> Unit)? = null

    override var historyItemClickListener: ((RadioStation, Int, Boolean) -> Unit)? = null

    override fun setOnClickListener(listener : (RadioStation, Int) -> Unit){
        onItemClickListener = listener
    }
    override fun setOnClickListener(listener: (RadioStation, Int, Boolean) -> Unit) {
        historyItemClickListener = listener
    }

}