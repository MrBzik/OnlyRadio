package com.example.radioplayer.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.ItemRadioWithTextBinding
import com.example.radioplayer.databinding.RadioItemBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.animations.AlphaFadeOutAnim
import com.example.radioplayer.ui.animations.fadeOut
import com.example.radioplayer.utils.RandomColors
import javax.inject.Inject

class PagingRadioAdapter @Inject constructor(
    private val glide : RequestManager

) : PagingDataAdapter<RadioStation, PagingRadioAdapter.RadioItemHolder>(StationsComparator) {

    private val randColors = RandomColors()



    class RadioItemHolder (itemView : View) : RecyclerView.ViewHolder(itemView)  {
        var bind : ItemRadioWithTextBinding
        init {
            bind = ItemRadioWithTextBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioItemHolder {
       val holder = RadioItemHolder(
           LayoutInflater.from(parent.context).inflate(R.layout.item_radio_with_text, parent, false)
       )
        holder.itemView.setOnClickListener {

            val item = getItem(holder.absoluteAdapterPosition)

            item?.let { station ->
                onItemClickListener?.let { click ->
                    click(station, holder.absoluteAdapterPosition)

                    updateOnStationChange(station, holder, true)
                }
            }
        }

        return holder

    }

    override fun onBindViewHolder(holder: RadioItemHolder, @SuppressLint("RecyclerView") position: Int) {
        val station = getItem(position)!!

        holder.bind.apply {

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
                        .listener(object : RequestListener<Drawable>{
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
                                        if(pos != holder.bindingAdapterPosition) {
                                            tvPlaceholder.alpha = alpha
                                        }
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
                        .into(ivItemImage)
                    }
                }


        if(station.stationuuid == currentRadioStationId){
            selectedAdapterPosition = holder.absoluteAdapterPosition
            previousItemHolder = holder
            handleStationPlaybackState(holder.bind)

        } else restoreState(holder.bind)

    }

    private var selectedAdapterPosition = -2

    var defaultTextColor = 0
    var selectedTextColor = 0

    var defaultSecondaryTextColor = 0
    var selectedSecondaryTextColor = 0

    var separatorDefault = 0

    var alpha = 0.1f

    var titleSize = 18f

    override fun onViewRecycled(holder: RadioItemHolder) {
        super.onViewRecycled(holder)
        glide.clear(holder.bind.ivItemImage)
    }


    private fun restoreState(bind: ItemRadioWithTextBinding){
        bind.apply {
            radioItemRootLayout.setBackgroundResource(R.color.main_background)
            tvPrimary.setTextColor(defaultTextColor)

            tvSecondary.setTextColor(defaultSecondaryTextColor)


            viewBottomSeparator?.setBackgroundColor(separatorDefault)



        }
    }

    private fun handleStationPlaybackState(bind: ItemRadioWithTextBinding){

        if(currentPlaybackState){
            bind.apply {
               radioItemRootLayout.setBackgroundResource(R.drawable.radio_selected_gradient)
               tvPrimary.setTextColor(selectedTextColor)

               tvSecondary.setTextColor(selectedSecondaryTextColor)

               viewBottomSeparator?.setBackgroundResource(R.color.station_bottom_separator_active)

            }

        } else {
            bind.apply {
                radioItemRootLayout.setBackgroundResource(R.drawable.radio_unselected_gradient)
                tvPrimary.setTextColor(defaultTextColor)

                tvSecondary.setTextColor(defaultSecondaryTextColor)


                viewBottomSeparator?.setBackgroundResource(R.color.station_bottom_separator_selected)

            }
        }
    }


    fun updateStationPlaybackState(){
        previousItemHolder?.let{
            if(it.absoluteAdapterPosition == selectedAdapterPosition){
                handleStationPlaybackState(it.bind)
            }
        }
    }


    fun updateOnStationChange(station : RadioStation, holder : RadioItemHolder?,
                              isClicked : Boolean = false
    ){

        if(station.stationuuid != currentRadioStationId) {

            currentRadioStationId = station.stationuuid
            previousItemHolder?.bind?.let {
                restoreState(it)
            }
        }
        holder?.let {
            selectedAdapterPosition = holder.absoluteAdapterPosition
            previousItemHolder = holder
            if(!isClicked){
                handleStationPlaybackState(holder.bind)
            }
        } ?: kotlin.run {
            selectedAdapterPosition = -2
        }
    }





    var currentRadioStationId : String? = null
    var currentPlaybackState = false
    var previousItemHolder : RadioItemHolder? = null




    private var onItemClickListener : ((RadioStation, Int) -> Unit)? = null

    fun setOnClickListener(listener : (RadioStation, Int) -> Unit){
        onItemClickListener = listener
    }



    object StationsComparator : DiffUtil.ItemCallback<RadioStation>(){

        override fun areItemsTheSame(oldItem: RadioStation, newItem: RadioStation): Boolean {
           return oldItem.stationuuid == newItem.stationuuid
        }

        override fun areContentsTheSame(oldItem: RadioStation, newItem: RadioStation): Boolean {
            return oldItem.stationuuid == newItem.stationuuid
        }
    }



}