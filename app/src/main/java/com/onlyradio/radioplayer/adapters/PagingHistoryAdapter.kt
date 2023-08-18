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
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.ui.animations.AdapterFadeAnim.adapterItemFadeIn
import com.onlyradio.radioplayer.utils.Utils
import java.text.DateFormat
import javax.inject.Inject


private const val TYPE_RADIO_STATION = 0
private const val TYPE_DATE_SEPARATOR = 1
private const val TYPE_DATE_SEPARATOR_ENCLOSING = 2


class PagingHistoryAdapter constructor(
   glide : RequestManager,
   private val todayStr : String,
) : PagingDataAdapter<StationWithDateModel, RecyclerView.ViewHolder>(StationsComparator) {

    val utils = BaseAdapter(glide)

    private val currentDate = Utils.convertLongToOnlyDate(System.currentTimeMillis(), DateFormat.LONG)

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

                    val item = getItem(holder.absoluteAdapterPosition) as StationWithDateModel.Station

                    utils.onItemClickListener?.let { click ->

                        click(item.radioStation, holder.absoluteAdapterPosition)
                    }

                    if(item.radioStation.stationuuid == currentRadioStationID){

                        if(selectedAdapterPosition != holder.absoluteAdapterPosition){

                            previousItemHolder?.bind?.let {
                                utils.restoreState(it)
                            }
                        }
                    }
                    else {

                        selectedAdapterPosition = holder.absoluteAdapterPosition
                        currentRadioStationID = item.radioStation.stationuuid

                        previousItemHolder?.bind?.let {

                            utils.restoreState(it)
                        }

                    }
                    selectedAdapterPosition = holder.absoluteAdapterPosition
                    previousItemHolder = holder
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


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val item = getItem(position)

        if(item is StationWithDateModel.DateSeparator)
            (holder as DateSeparatorViewHolder).apply {
                if(currentDate == item.date){
                    bind.tvDate.text = todayStr
                } else {
                    bind.tvDate.text = item.date
                }
            }

        else if(item is StationWithDateModel.DateSeparatorEnclosing)
            (holder as DateSeparatorEnclosingViewHolder).apply {
                if(currentDate == item.date){
                    bind.tvDate.text = todayStr
                } else {
                    bind.tvDate.text = item.date
                }
            }

        else if (item is StationWithDateModel.Station){
            (holder as StationViewHolder).bind.apply {

                val station = item.radioStation

                utils.handleBinding(this, station, position){
                    holder.bindingAdapterPosition
                }


                if(station.stationuuid == currentRadioStationID){

                        previousItemHolder?.bind?.let {
                            utils.restoreState(it)
                        }

                        previousItemHolder = holder
                        selectedAdapterPosition = holder.absoluteAdapterPosition
                        utils.handleStationPlaybackState(this)

                } else

                    utils.restoreState(this)

            }
        }

        adapterItemFadeIn(holder.itemView)

    }

//    var separatorDefault = 0


    fun updateStationPlaybackState(){
       previousItemHolder?.let{
           if(it.absoluteAdapterPosition == selectedAdapterPosition){
               utils.handleStationPlaybackState(it.bind)
           }
       }
    }


    fun updateOnStationChange(station : RadioStation,
                              holder : StationViewHolder?
    ){
        if(station.stationuuid != currentRadioStationID) {

            currentRadioStationID = station.stationuuid
            previousItemHolder?.bind?.let {
                utils.restoreState(it)
            }
        }
        holder?.let {
            selectedAdapterPosition = holder.absoluteAdapterPosition
            previousItemHolder = holder

            utils.handleStationPlaybackState(holder.bind)

        }
    }


    var currentRadioStationID : String? = null
    private var selectedAdapterPosition = -2

    private var previousItemHolder : StationViewHolder? = null



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