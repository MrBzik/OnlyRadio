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
import com.onlyradio.radioplayer.utils.RandomColors


class BaseAdapter(
    private val glide : RequestManager) {

    private val randColors = RandomColors()

    var currentPlaybackState = false
    var defaultTextColor = 0
    var selectedTextColor = 0
    var defaultSecondaryTextColor = 0
    var selectedSecondaryTextColor = 0

    var alpha = 0.1f
    var titleSize = 18f

    private val defaultBG = R.drawable.item_radio_bg_default
    private val playingBG = R.drawable.item_radio_bg_playing
    private val pausedBG = R.drawable.item_radio_bg_paused


    fun initialiseValues(context : Context, fontSize : Float){
        defaultTextColor = ContextCompat.getColor(context, R.color.default_text_color)
        selectedTextColor = ContextCompat.getColor(context, R.color.selected_text_color)

        defaultSecondaryTextColor = ContextCompat.getColor(context, R.color.default_secondary_text_color)
        selectedSecondaryTextColor = ContextCompat.getColor(context, R.color.selected_secondary_text_color)
        alpha = context.resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
        titleSize = fontSize
    }

     fun restoreState(bind: ItemRadioWithTextBinding){
        bind.apply {
            radioItemRootLayout.setBackgroundResource(defaultBG)
            tvPrimary.setTextColor(defaultTextColor)

            tvSecondary.setTextColor(defaultSecondaryTextColor)

        }
    }

     fun handleStationPlaybackState(bind: ItemRadioWithTextBinding){

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

    fun handleBinding(
        bind: ItemRadioWithTextBinding,
        station: RadioStation,
        position : Int,
        saveImage : (Drawable) -> Unit = {},
        checkPosition : () -> Int
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
                    position = position,
                    saveImage = saveImage
                ){
                    checkPosition()
                }
            }
        }
    }

    var onItemClickListener : ((RadioStation, Int) -> Unit)? = null

    fun setOnClickListener(listener : (RadioStation, Int) -> Unit){
        onItemClickListener = listener
    }

}



