package com.example.radioplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.RadioItemBinding
import javax.inject.Inject

class RadioAdapter @Inject constructor(
    private val glide : RequestManager
) : RecyclerView.Adapter<RadioAdapter.RadioItemHolder>() {

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
        val station = listOfStations[position]

        holder.bind.apply {

            tvPrimary.text = station.name
            tvSecondary.text = station.country
            glide.load(station.favicon).into(ivItemImage)
        }

        holder.itemView.setOnClickListener {

            onItemClickListener?.let { click ->

                click(station)

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
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun check (){


    }

    var listOfStations : List<RadioStation>
    get() = differ.currentList
    set(value) = differ.submitList(value)


}