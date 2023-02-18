package com.example.radioplayer.adapters

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Point
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.RadioItemBinding
import javax.inject.Inject

class RadioDatabaseAdapter @Inject constructor(
    private val glide : RequestManager,
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
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivItemImage)
        }

        holder.bind.ivItemImage.setOnClickListener {

            onItemClickListener?.let { click ->

                click(station)

            }
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
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var listOfStations : List<RadioStation>
    get() = differ.currentList
    set(value) = differ.submitList(value)


}

