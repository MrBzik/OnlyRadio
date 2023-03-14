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
                    click(station)
                }
                if(station.name != currentRadioStationName) {
                    currentRadioStationName = station.name!!
                    previousItemHolder?.bind?.let {
                        restoreState(it)
                    }
                }
                previousItemHolder = holder
            }
        }

        return holder

    }

    override fun onBindViewHolder(holder: RadioItemHolder, @SuppressLint("RecyclerView") position: Int) {
        val station = getItem(position)!!

        holder.bind.apply {


            tvPrimary.text = station.name
            tvSecondary.apply {
                if(station.country?.isNotBlank() == true){
                    visibility = View.VISIBLE
                    text = station.country
                }

                else visibility = View.GONE
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
                    tvPlaceholder.setTextColor(color)
                    tvPlaceholder.alpha = 0.6f
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

                                    tvPlaceholder.fadeOut(300, 0.6f, position){ pos ->
                                        if(pos != holder.bindingAdapterPosition) {
                                            tvPlaceholder.alpha = 0.6f
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


        if(station.name == currentRadioStationName){

            previousItemHolder = holder
            handleStationPlaybackState(holder.bind)

        } else restoreState(holder.bind)

    }

    var defaultTextColor = 0
    var selectedTextColor = 0

    override fun onViewRecycled(holder: RadioItemHolder) {
        super.onViewRecycled(holder)
        glide.clear(holder.bind.ivItemImage)
    }


    private fun restoreState(bind: ItemRadioWithTextBinding){
        bind.apply {
            radioItemRootLayout.setBackgroundResource(R.color.main_background)
            tvPrimary.setTextColor(defaultTextColor)
            tvPrimary.alpha = 0.7f
            tvSecondary.setTextColor(defaultTextColor)
        }
    }

    private fun handleStationPlaybackState(bind: ItemRadioWithTextBinding){
        if(currentPlaybackState){
            bind.apply {
               radioItemRootLayout.setBackgroundResource(R.drawable.radio_selected_gradient)
               tvPrimary.setTextColor(selectedTextColor)
               tvPrimary.alpha = 0.9f
               tvSecondary.setTextColor(selectedTextColor)
            }

        } else {
            bind.apply {
                radioItemRootLayout.setBackgroundResource(R.drawable.radio_selected_gradient)
                tvPrimary.setTextColor(defaultTextColor)
                tvPrimary.alpha = 0.7f
                tvSecondary.setTextColor(defaultTextColor)
            }
        }
    }


    fun updateStationPlaybackState(){
        previousItemHolder?.bind?.let{
            if(it.tvPrimary.text == currentRadioStationName){
                handleStationPlaybackState(it)
            }
        }
    }

    var currentRadioStationName : String? = null
    var currentPlaybackState = false
    var previousItemHolder : RadioItemHolder? = null




    private var onItemClickListener : ((RadioStation) -> Unit)? = null

    fun setOnClickListener(listener : (RadioStation) -> Unit){
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