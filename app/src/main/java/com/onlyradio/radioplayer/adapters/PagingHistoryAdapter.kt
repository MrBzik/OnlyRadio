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
import com.onlyradio.radioplayer.utils.Logger
import com.onlyradio.radioplayer.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import javax.inject.Inject


private const val TYPE_RADIO_STATION = 0
private const val TYPE_DATE_SEPARATOR = 1
private const val TYPE_DATE_SEPARATOR_ENCLOSING = 2


class PagingHistoryAdapter constructor(
   private val glide : RequestManager,
   private val todayStr : String,
) : PagingDataAdapter<StationWithDateModel, RecyclerView.ViewHolder>(StationsComparator), AdapterUtils by AdapterUtilsImpl() {

    private val currentDate = Utils.convertLongToOnlyDate(System.currentTimeMillis(), DateFormat.LONG)

    var currentRadioStationID : String? = null
    private var selectedAdapterPosition = -2

    private var previousItemHolder : StationViewHolder? = null

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

                        onItemClickListener?.let { click ->

                            click(item.radioStation, holder.bindingAdapterPosition)
                        }

                        if(item.radioStation.stationuuid == currentRadioStationID){

                            if(selectedAdapterPosition != holder.bindingAdapterPosition){

                                previousItemHolder?.bind?.let {
                                    restoreState(it)
                                }
                            }
                        }
                        else {

                            selectedAdapterPosition = holder.bindingAdapterPosition
                            currentRadioStationID = item.radioStation.stationuuid

                            previousItemHolder?.bind?.let {

                                restoreState(it)
                            }

                        }
                        selectedAdapterPosition = holder.bindingAdapterPosition
                        previousItemHolder = holder
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


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {

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


                    if(station.stationuuid == currentRadioStationID){

                        previousItemHolder?.bind?.let {
                            restoreState(it)
                        }

                        previousItemHolder = holder
                        selectedAdapterPosition = holder.bindingAdapterPosition
                        handleStationPlaybackState(this)

                    } else

                        restoreState(this)

                }
            }
        }

        adapterItemFadeIn(holder.itemView)

    }

//    var separatorDefault = 0


    fun updateStationPlaybackState(){
       previousItemHolder?.let{
           if(it.bindingAdapterPosition == selectedAdapterPosition){
               handleStationPlaybackState(it.bind)
           }
       }
    }


    fun updateOnStationChange(stationId : String,
                              holder : StationViewHolder?
    ){
        if(stationId != currentRadioStationID) {
            currentRadioStationID = stationId
            previousItemHolder?.bind?.let {
                restoreState(it)
            }
        }
        holder?.let {
            selectedAdapterPosition = holder.bindingAdapterPosition
            previousItemHolder = holder

            handleStationPlaybackState(holder.bind)

        }
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