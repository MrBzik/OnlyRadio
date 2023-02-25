package com.example.radioplayer.adapters

import android.graphics.Color
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.RadioItemBinding
import java.lang.String
import javax.inject.Inject

class PagingRadioAdapter @Inject constructor(
    private val glide : RequestManager,
    private val glideFactory: DrawableCrossFadeFactory

) : PagingDataAdapter<RadioStation, PagingRadioAdapter.RadioItemHolder>(StationsComparator) {


    class RadioItemHolder (itemView : View) : RecyclerView.ViewHolder(itemView)  {
        var bind : RadioItemBinding
        init {
            bind = RadioItemBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioItemHolder {
       return RadioItemHolder(
           LayoutInflater.from(parent.context).inflate(R.layout.radio_item, parent, false)
       )

    }

    override fun onBindViewHolder(holder: RadioItemHolder, position: Int) {
        val station = getItem(position)!!

        holder.bind.apply {

            tvPrimary.text = station.name
            tvSecondary.text = station.country
            glide
                .load(station.favicon)
                .placeholder(R.drawable.ic_radio_default)
                .transition(withCrossFade(glideFactory))
                .into(ivItemImage)
        }

        holder.bind.ivItemImage.setOnClickListener {

            onItemClickListener?.let { click ->
                click(station)
            }

            if(station.name == currentRadioStationName){/*DO NOTHING*/}
            else {
                currentRadioStationName = station.name!!
                previousItemHolder?.bind?.let {
                    restoreState(it)
                }
            }
            previousItemHolder = holder
        }

        if(station.name == currentRadioStationName){

            previousItemHolder = holder
            handleStationPlaybackState(holder.bind)

        } else
            restoreState(holder.bind)

    }

    var defaultTextColor = 0
    var selectedTextColor = 0




    private fun restoreState(bind: RadioItemBinding){
        bind.apply {
            radioItemRootLayout.setBackgroundResource(R.color.main_background)
            tvPrimary.setTextColor(defaultTextColor)
            tvPrimary.alpha = 0.7f
            tvSecondary.setTextColor(defaultTextColor)
        }
    }

    private fun handleStationPlaybackState(bind: RadioItemBinding){
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

    var currentRadioStationName = ""
    var currentPlaybackState = false
    private var previousItemHolder : RadioItemHolder? = null




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