package com.example.radioplayer.adapters

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.ItemRadioWithTextBinding
import com.example.radioplayer.ui.animations.AdapterAnimator
import com.example.radioplayer.ui.animations.fadeOut
import com.example.radioplayer.utils.RandomColors
import javax.inject.Inject

class PagingRadioAdapter @Inject constructor(
    private val glide : RequestManager

) : PagingDataAdapter<RadioStation, PagingRadioAdapter.RadioItemHolder>(StationsComparator) {

    val utils = BaseAdapter(glide)

    val animator = AdapterAnimator()

    class RadioItemHolder (val bind : ItemRadioWithTextBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioItemHolder {
       val holder = RadioItemHolder(
           ItemRadioWithTextBinding.inflate(
               LayoutInflater.from(parent.context), parent, false
           )
       )
        holder.itemView.setOnClickListener {

            val item = getItem(holder.absoluteAdapterPosition)

            item?.let { station ->
                utils.onItemClickListener?.let { click ->
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

            utils.handleBinding(this, station, position){pos ->
                if(pos != holder.bindingAdapterPosition) {
                    tvPlaceholder.alpha = utils.alpha
                }
            }
        }

        if(station.stationuuid == currentRadioStationId){
            selectedAdapterPosition = holder.absoluteAdapterPosition
            previousItemHolder = holder
            utils.handleStationPlaybackState(holder.bind)

        } else utils.restoreState(holder.bind)

        animator.animateAppearance(holder.itemView)

    }

    private var selectedAdapterPosition = -2


    override fun onViewRecycled(holder: RadioItemHolder) {
        super.onViewRecycled(holder)
        glide.clear(holder.bind.ivItemImage)
    }


    fun updateStationPlaybackState(){
        previousItemHolder?.let{
            if(it.absoluteAdapterPosition == selectedAdapterPosition){
                utils.handleStationPlaybackState(it.bind)
            }
        }
    }


    fun updateOnStationChange(station : RadioStation, holder : RadioItemHolder?,
                              isClicked : Boolean = false
    ){

        if(station.stationuuid != currentRadioStationId) {

            currentRadioStationId = station.stationuuid
            previousItemHolder?.bind?.let {
                utils.restoreState(it)
            }
        }
        holder?.let {
            selectedAdapterPosition = holder.absoluteAdapterPosition
            previousItemHolder = holder
            if(!isClicked){
                utils.handleStationPlaybackState(holder.bind)
            }
        } ?: kotlin.run {
            selectedAdapterPosition = -2
        }
    }

    var currentRadioStationId : String? = null

    var previousItemHolder : RadioItemHolder? = null



    object StationsComparator : DiffUtil.ItemCallback<RadioStation>(){

        override fun areItemsTheSame(oldItem: RadioStation, newItem: RadioStation): Boolean {
           return oldItem.stationuuid == newItem.stationuuid
        }

        override fun areContentsTheSame(oldItem: RadioStation, newItem: RadioStation): Boolean {
            return oldItem.stationuuid == newItem.stationuuid
        }
    }

}