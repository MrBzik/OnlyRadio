package com.onlyradio.radioplayer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.databinding.ItemRadioWithTextBinding
import com.onlyradio.radioplayer.ui.animations.AdapterFadeAnim.adapterItemFadeIn
import javax.inject.Inject

class PagingRadioAdapter @Inject constructor(
    private val glide : RequestManager

) : PagingDataAdapter<RadioStation, PagingRadioAdapter.RadioItemHolder>(StationsComparator) {


    val utils = BaseAdapter(glide)

    var currentRadioStationId : String? = null

    var previousItemHolder : RadioItemHolder? = null

    class RadioItemHolder (val bind : ItemRadioWithTextBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioItemHolder {
       val holder = RadioItemHolder(
           ItemRadioWithTextBinding.inflate(
               LayoutInflater.from(parent.context), parent, false
           )
       )
        holder.itemView.setOnClickListener {

            if(holder.bindingAdapterPosition >= 0){

                val item = getItem(holder.bindingAdapterPosition)

                item?.let { station ->
                    utils.onItemClickListener?.let { click ->
                        click(station, holder.bindingAdapterPosition)

                        updateOnStationChange(station, holder, true)
                    }
                }
            }
        }

        return holder

    }

    override fun onBindViewHolder(holder: RadioItemHolder, @SuppressLint("RecyclerView") position: Int) {

        val station = getItem(position)!!

        holder.bind.apply {

            utils.handleBinding(
                bind = this,
                station = station,
                position = position)
               {
                holder.bindingAdapterPosition
            }
        }

        if(station.stationuuid == currentRadioStationId){
            selectedAdapterPosition = holder.bindingAdapterPosition
            previousItemHolder = holder
            utils.handleStationPlaybackState(holder.bind)

        } else utils.restoreState(holder.bind)

        adapterItemFadeIn(holder.itemView)

    }

    private var selectedAdapterPosition = -2


    override fun onViewRecycled(holder: RadioItemHolder) {
        super.onViewRecycled(holder)
        glide.clear(holder.bind.ivItemImage)
    }


    fun updateStationPlaybackState(){
        previousItemHolder?.let{
            if(it.bindingAdapterPosition == selectedAdapterPosition){
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
            selectedAdapterPosition = holder.bindingAdapterPosition
            previousItemHolder = holder
            if(!isClicked){
                utils.handleStationPlaybackState(holder.bind)
            }
        } ?: kotlin.run {
            selectedAdapterPosition = -2
        }
    }




    object StationsComparator : DiffUtil.ItemCallback<RadioStation>(){

        override fun areItemsTheSame(oldItem: RadioStation, newItem: RadioStation): Boolean {
           return oldItem.stationuuid == newItem.stationuuid
        }

        override fun areContentsTheSame(oldItem: RadioStation, newItem: RadioStation): Boolean {
            return oldItem.stationuuid == newItem.stationuuid
        }
    }

    fun reset(){
        previousItemHolder = null
        currentRadioStationId = null
    }

}



//saveImage = {drawable ->
//
//    try {
//        val bitmap = (drawable as BitmapDrawable).bitmap
//
//        val path = holder.itemView.context.filesDir.absolutePath + File.separator
//
//        val file = File(path)
//
//        val imageFile = File(file, System.currentTimeMillis().toString() + ".png")
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val out = FileOutputStream(imageFile)
//
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
//
//            out.close()
//        }
//    } catch (e : Exception){
//
//    }
//
//
//}
//)