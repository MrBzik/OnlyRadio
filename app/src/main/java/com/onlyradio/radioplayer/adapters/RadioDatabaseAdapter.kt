package com.onlyradio.radioplayer.adapters

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.databinding.ItemRadioWithTextBinding
import com.onlyradio.radioplayer.ui.animations.AdapterFadeAnim.adapterItemFadeIn
import com.onlyradio.radioplayer.ui.fragments.FavStationsFragment
import javax.inject.Inject

class RadioDatabaseAdapter @Inject constructor(
    private val glide : RequestManager
) : RecyclerView.Adapter<RadioDatabaseAdapter.RadioItemHolder>(), AdapterUtils by AdapterUtilsImpl() {


    class RadioItemHolder (val bind : ItemRadioWithTextBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioItemHolder {
       val holder = RadioItemHolder(
            ItemRadioWithTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
       )

        holder.itemView.setOnLongClickListener {
            val station =  listOfStations[holder.bindingAdapterPosition]
            val clipText = station.stationuuid
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData("STATION_ID", mimeTypes, item)
            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)
            FavStationsFragment.dragAndDropItemPos = holder.bindingAdapterPosition
            FavStationsFragment.dragAndDropStation = station
            true
        }


        holder.itemView.setOnClickListener {

            if(holder.bindingAdapterPosition >= 0){
                val station = listOfStations[holder.bindingAdapterPosition]
                onItemClickListener?.let { click ->
                    click(station, holder.bindingAdapterPosition)

                    updateOnStationChange(station, holder, true)
                }
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: RadioItemHolder, @SuppressLint("RecyclerView") position: Int) {
        val station = listOfStations[position]

        holder.bind.apply {
             handleBinding(
                 bind = holder.bind,
                 station = station,
                 position = position,
                 checkPosition = {
                     holder.bindingAdapterPosition
                 },
                 glide = glide
             )
        }

        if(station.stationuuid == currentRadioStationId) {
            selectedAdapterPosition = holder.bindingAdapterPosition
            previousItemHolder = holder
            handleStationPlaybackState(holder.bind)
        } else
            restoreState(holder.bind)

        adapterItemFadeIn(holder.itemView)

    }

    private var selectedAdapterPosition = -2


//    var separatorDefault = 0

    var currentRadioStationId : String? = null

    private var previousItemHolder : RadioItemHolder? = null




    fun updateStationPlaybackState(){
        previousItemHolder?.let{
            if(it.bindingAdapterPosition == selectedAdapterPosition){
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
            selectedAdapterPosition = holder.bindingAdapterPosition
            previousItemHolder = holder
            if(!isClicked){
                handleStationPlaybackState(holder.bind)
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

