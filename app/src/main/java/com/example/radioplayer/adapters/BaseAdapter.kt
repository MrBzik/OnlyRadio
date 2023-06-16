package com.example.radioplayer.adapters

import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.ItemRadioWithTextBinding
import com.example.radioplayer.ui.animations.fadeOut
import com.example.radioplayer.utils.RandomColors



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
        checkPosition : (Int) -> Unit
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

                glide
                    .load(station.favicon)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {

                            return true
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {

                            if(dataSource?.name == "REMOTE"){

                                tvPlaceholder.fadeOut(300, alpha, position){ pos ->
                                   checkPosition(pos)
                                }

                            }
                            else {

                                tvPlaceholder.alpha = 0f
                            }
                            ivItemImage.visibility = View.VISIBLE
                            return false
                        }
                    })
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(RequestOptions().override(65, 65))
                    .into(ivItemImage)
            }
        }
    }

    var onItemClickListener : ((RadioStation, Int) -> Unit)? = null

    fun setOnClickListener(listener : (RadioStation, Int) -> Unit){
        onItemClickListener = listener
    }




}

