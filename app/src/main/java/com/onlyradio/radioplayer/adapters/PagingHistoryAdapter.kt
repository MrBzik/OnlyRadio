package com.onlyradio.radioplayer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.adapters.models.StationWithDateModel
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.databinding.ItemDateSeparatorBinding
import com.onlyradio.radioplayer.databinding.ItemDateSeparatorEnclosingBinding
import com.onlyradio.radioplayer.databinding.ItemRadioWithTextBinding
import com.onlyradio.radioplayer.ui.animations.AdapterFadeAnim.adapterItemFadeIn
import com.onlyradio.radioplayer.utils.Logger
import com.onlyradio.radioplayer.utils.Utils
import java.text.DateFormat


private const val TYPE_RADIO_STATION = 0
private const val TYPE_DATE_SEPARATOR = 1
private const val TYPE_DATE_SEPARATOR_ENCLOSING = 2


class PagingHistoryAdapter constructor(
   private val glide : RequestManager,
   private val todayStr : String,
) : PagingDataAdapter<StationWithDateModel, RecyclerView.ViewHolder>(StationsComparator), AdapterUtils by AdapterUtilsImpl() {

    private val currentDate = Utils.convertLongToOnlyDate(System.currentTimeMillis(), DateFormat.LONG)

    private var selectedRadioStationId : String? = null
    private var selectedAdapterPosition = -2


    class StationViewHolder (val bind: ItemRadioWithTextBinding)
        : RecyclerView.ViewHolder(bind.root)

    class DateSeparatorViewHolder (val bind : ItemDateSeparatorBinding)
        : RecyclerView.ViewHolder(bind.root)

    class DateSeparatorEnclosingViewHolder (val bind : ItemDateSeparatorEnclosingBinding)
        : RecyclerView.ViewHolder(bind.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when(viewType){

            TYPE_RADIO_STATION -> {
                val holder = StationViewHolder(
                    ItemRadioWithTextBinding.inflate(
                        LayoutInflater.from(parent.context),  parent, false
                    )
                )

                holder.itemView.setOnClickListener {

                    if(holder.bindingAdapterPosition >= 0){

                        val item = getItem(holder.bindingAdapterPosition) as StationWithDateModel.Station

                        historyItemClickListener?.let { click ->

                            var isToShift = false

                            if(item.radioStation.stationuuid == selectedRadioStationId){
                                if(holder.bindingAdapterPosition != selectedAdapterPosition){
                                    isToShift = true
                                }
                            }

                            click(item.radioStation, holder.bindingAdapterPosition, isToShift)
                        }
                    }
                }

                return holder

            }
            TYPE_DATE_SEPARATOR ->
                DateSeparatorViewHolder(
                    ItemDateSeparatorBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            else ->
                DateSeparatorEnclosingViewHolder(
                    ItemDateSeparatorEnclosingBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
        }
    }


    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        val item = getItem(position) ?: return

        when (item) {
            is StationWithDateModel.DateSeparator -> (holder as DateSeparatorViewHolder).apply {
                if(currentDate == item.date){
                    bind.tvDate.text = todayStr
                } else {
                    bind.tvDate.text = item.date
                }
            }

            is StationWithDateModel.DateSeparatorEnclosing -> (holder as DateSeparatorEnclosingViewHolder).apply {
                if(currentDate == item.date){
                    bind.tvDate.text = todayStr
                } else {
                    bind.tvDate.text = item.date
                }
            }

            is StationWithDateModel.Station -> {

                (holder as StationViewHolder).bind.apply {

                    val station = item.radioStation

                    handleBinding(
                        bind = this,
                        station = station,
                        position = position,
                        glide = glide,
                        checkPosition = {
                            holder.bindingAdapterPosition
                        }
                    )

                    if(station.stationuuid == selectedRadioStationId){
                        if(holder.bindingAdapterPosition != selectedAdapterPosition){

                            restoreState?.let {
                                it(selectedAdapterPosition, holder.bindingAdapterPosition)
                            }
                            selectedAdapterPosition = holder.bindingAdapterPosition
                        }

                        handleStationPlaybackState(this)

                    } else restoreState(this)
                }
            }
        }

        adapterItemFadeIn(holder.itemView)

    }



    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int,
        payloads: MutableList<Any>
    ) {

        if(payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {

            if(holder is StationViewHolder){
                if(payloads[0] is Int){
                    handleStationPlaybackState(holder.bind)
                } else restoreState(holder.bind)
            }

//            val item = getItem(position) ?: return
//
//            if(item is StationWithDateModel.Station){
//
//                (holder as StationViewHolder).apply {
//                    if(payloads[0] is Int){
//                        if(selectedRadioStationId == item.radioStation.stationuuid){
//                            handleStationPlaybackState(holder.bind)
//                        }
//                    } else restoreState(holder.bind)
//                }
//            }
        }
    }

//    var separatorDefault = 0

    private var restoreState : ((oldPos : Int, newPos : Int) -> Unit)? = null
    fun restoreState(onRestore : (oldPos : Int, newPos : Int) -> Unit){
        restoreState = onRestore
    }

    fun handleRestoreState(pos : Int){
        notifyItemChanged(pos, 1f)
    }

    fun onNewFlow(index: Int){
        selectedAdapterPosition = index
    }

    fun getForSameItem(id: String) : Int? {
        if(id == selectedRadioStationId && selectedAdapterPosition >= 0)
            return selectedAdapterPosition
        return null
    }

    fun onNewPlayingItem(newIndex : Int, id : String, isPlaying: Boolean){
        currentPlaybackState = isPlaying
        if(selectedAdapterPosition >= 0 && selectedAdapterPosition != newIndex)
            notifyItemChanged(selectedAdapterPosition, 1f)
        selectedAdapterPosition = newIndex
        selectedRadioStationId = id
        notifyItemChanged(newIndex, 1)
    }



    override fun getItemViewType(position: Int): Int {

        return when(getItem(position)){
            is StationWithDateModel.Station -> TYPE_RADIO_STATION
            is StationWithDateModel.DateSeparator -> TYPE_DATE_SEPARATOR
            else -> TYPE_DATE_SEPARATOR_ENCLOSING
        }
    }


   object StationsComparator : DiffUtil.ItemCallback<StationWithDateModel>(){

        override fun areItemsTheSame(
            oldItem: StationWithDateModel, newItem: StationWithDateModel
        ): Boolean {

            val isSameStation = oldItem is StationWithDateModel.Station
                    && newItem is StationWithDateModel.Station
                    && oldItem.radioStation.stationuuid == newItem.radioStation.stationuuid

            val isSameSeparator = oldItem is StationWithDateModel.DateSeparator
                    && newItem is StationWithDateModel.DateSeparator
                    && oldItem.date == newItem.date

            val isSameSeparatorEnclosing = oldItem is StationWithDateModel.DateSeparatorEnclosing
                    && newItem is StationWithDateModel.DateSeparatorEnclosing
                    && oldItem.date == newItem.date

            return isSameStation || isSameSeparator || isSameSeparatorEnclosing

        }

        override fun areContentsTheSame(
            oldItem: StationWithDateModel, newItem: StationWithDateModel
        ): Boolean {

            val isSameStation = oldItem is StationWithDateModel.Station
                    && newItem is StationWithDateModel.Station
                    && oldItem.radioStation.stationuuid == newItem.radioStation.stationuuid

            val isSameSeparator = oldItem is StationWithDateModel.DateSeparator
                    && newItem is StationWithDateModel.DateSeparator
                    && oldItem.date == newItem.date

            val isSameSeparatorEnclosing = oldItem is StationWithDateModel.DateSeparatorEnclosing
                    && newItem is StationWithDateModel.DateSeparatorEnclosing
                    && oldItem.date == newItem.date

            return isSameStation || isSameSeparator || isSameSeparatorEnclosing

        }
    }


}