package com.example.radioplayer.adapters

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.AsyncListDiffer
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
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.animations.AdapterAnimator
import com.example.radioplayer.ui.animations.fadeOut
import com.example.radioplayer.ui.fragments.FavStationsFragment
import com.example.radioplayer.utils.RandomColors
import javax.inject.Inject

class RadioDatabaseAdapter @Inject constructor(
    glide : RequestManager
) : RecyclerView.Adapter<RadioDatabaseAdapter.RadioItemHolder>() {

    val utils = BaseAdapter(glide)

    val animator = AdapterAnimator()

    class RadioItemHolder (val bind : ItemRadioWithTextBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioItemHolder {
       val holder = RadioItemHolder(
            ItemRadioWithTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
       )

        holder.itemView.setOnLongClickListener {
            val station =  listOfStations[holder.absoluteAdapterPosition]
            val clipText = station.stationuuid
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData("STATION_ID", mimeTypes, item)
            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)
            FavStationsFragment.dragAndDropItemPos = holder.absoluteAdapterPosition
            FavStationsFragment.dragAndDropStation = station
            true
        }


        holder.itemView.setOnClickListener {
            val station = listOfStations[holder.absoluteAdapterPosition]
            utils.onItemClickListener?.let { click ->
                click(station, holder.absoluteAdapterPosition)

                updateOnStationChange(station, holder, true)
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: RadioItemHolder, @SuppressLint("RecyclerView") position: Int) {
        val station = listOfStations[position]

        holder.bind.apply {
           utils.handleBinding(holder.bind, station, position){
               holder.bindingAdapterPosition
           }
        }

        if(station.stationuuid == currentRadioStationId) {
            selectedAdapterPosition = holder.absoluteAdapterPosition
            previousItemHolder = holder
            utils.handleStationPlaybackState(holder.bind)
        } else
            utils.restoreState(holder.bind)

        animator.animateAppearance(holder.itemView)

    }

    private var selectedAdapterPosition = -2


//    var separatorDefault = 0

    var currentRadioStationId : String? = null

    private var previousItemHolder : RadioItemHolder? = null




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


    override fun getItemCount(): Int {
       return listOfStations.size
    }




    private val diffCallback = object : DiffUtil.ItemCallback<RadioStation>(){

        override fun areItemsTheSame(oldItem: RadioStation, newItem: RadioStation): Boolean {
           return oldItem.stationuuid == newItem.stationuuid
        }

        override fun areContentsTheSame(oldItem: RadioStation, newItem: RadioStation): Boolean {
            return oldItem.stationuuid == newItem.stationuuid
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var listOfStations : List<RadioStation>
    get() = differ.currentList
    set(value) = differ.submitList(value)


}

