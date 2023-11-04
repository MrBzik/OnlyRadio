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

) : PagingDataAdapter<RadioStation, PagingRadioAdapter.RadioItemHolder>(StationsComparator), AdapterUtils by AdapterUtilsImpl() {


    private var selectedRadioStationId = ""

    private var selectedAdapterPosition = -2


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
                    onItemClickListener?.let { click ->
                        click(station, holder.bindingAdapterPosition)
                    }
                }
            }
        }

        return holder

    }


    override fun onBindViewHolder(
        holder: RadioItemHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {

        if(payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            restoreState(holder.bind)
        }
    }



    override fun onBindViewHolder(holder: RadioItemHolder, @SuppressLint("RecyclerView") position: Int) {
        val station = getItem(position)!!

        holder.bind.apply {
            handleBinding(
                bind = this,
                station = station,
                position = position,
                glide = glide,
                checkPosition = {
                    holder.bindingAdapterPosition
                }
            )

        }

        if(station.stationuuid == selectedRadioStationId){

            handleStationPlaybackState(holder.bind)

        } else restoreState(holder.bind)

        adapterItemFadeIn(holder.itemView)

    }


    override fun onViewRecycled(holder: RadioItemHolder) {
        super.onViewRecycled(holder)
        glide.clear(holder.bind.ivItemImage)
    }

    fun updateSelectedItemValues(index : Int, id : String, isPlaying: Boolean){
        currentPlaybackState = isPlaying
        selectedAdapterPosition = index
        selectedRadioStationId = id
    }


    fun onNewPlayingItem(newIndex : Int, id : String, isPlaying : Boolean, holder: RadioItemHolder){
        if(selectedAdapterPosition >= 0 && selectedAdapterPosition != newIndex)
            notifyItemChanged(selectedAdapterPosition, 1)
        updateSelectedItemValues(newIndex, id, isPlaying)
        handleStationPlaybackState(holder.bind)
    }

    fun isSameId(id: String) : Boolean {
        return selectedRadioStationId == id
    }

    fun restoreState(){
        selectedRadioStationId = ""
        selectedAdapterPosition = -2
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