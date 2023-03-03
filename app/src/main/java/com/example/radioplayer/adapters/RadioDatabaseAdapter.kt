package com.example.radioplayer.adapters

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Point
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.RadioItemBinding
import javax.inject.Inject

class RadioDatabaseAdapter @Inject constructor(
    private val glide : RequestManager,
    private val glideFactory : DrawableCrossFadeFactory
) : RecyclerView.Adapter<RadioDatabaseAdapter.RadioItemHolder>() {

    class RadioItemHolder (val bind : RadioItemBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioItemHolder {
       return RadioItemHolder(
            RadioItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
       )
    }

    override fun onBindViewHolder(holder: RadioItemHolder, position: Int) {
        val station = listOfStations[position]

        holder.bind.apply {

            tvPrimary.text = station.name
            tvSecondary.text = station.country
            glide
                .load(station.favicon)
                .placeholder(R.drawable.ic_radio_default)
                .transition(withCrossFade(glideFactory))
                .into(ivItemImage)
        }

        if(station.name == currentRadioStationName){

            previousItemHolder = holder
            handleStationPlaybackState(holder.bind)

        } else
            restoreState(holder.bind)


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

        holder.itemView.setOnLongClickListener{
                val clipText = station.stationuuid
                val item = ClipData.Item(clipText)
                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                val data = ClipData("STATION_ID", mimeTypes, item)
                val dragShadowBuilder = View.DragShadowBuilder(it)
                it.startDragAndDrop(data, dragShadowBuilder, it, 0)
            true
        }
    }

    var defaultTextColor = 0
    var selectedTextColor = 0
    var currentRadioStationName = ""
    var currentPlaybackState = false
    private var previousItemHolder : RadioItemHolder? = null

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



    private var onItemClickListener : ((RadioStation) -> Unit)? = null

    fun setOnClickListener(listener : (RadioStation) -> Unit){
        onItemClickListener = listener
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

