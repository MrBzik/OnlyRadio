package com.example.radioplayer.adapters

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.radioplayer.R
import com.example.radioplayer.adapters.models.StationWithDateModel
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.ItemDateSeparatorBinding
import com.example.radioplayer.databinding.ItemDateSeparatorEnclosingBinding
import com.example.radioplayer.databinding.RadioItemBinding
import javax.inject.Inject


private const val TYPE_RADIO_STATION = 0
private const val TYPE_DATE_SEPARATOR = 1
private const val TYPE_DATE_SEPARATOR_ENCLOSING = 2


class PagingHistoryAdapter @Inject constructor(
    private val glide : RequestManager,
    private val glideFactory : DrawableCrossFadeFactory

) : PagingDataAdapter<StationWithDateModel, RecyclerView.ViewHolder>(StationsComparator) {



    class StationViewHolder (val bind: RadioItemBinding)
        : RecyclerView.ViewHolder(bind.root)

    class DateSeparatorViewHolder (val bind : ItemDateSeparatorBinding)
        : RecyclerView.ViewHolder(bind.root)

    class DateSeparatorEnclosingViewHolder (val bind : ItemDateSeparatorEnclosingBinding)
        : RecyclerView.ViewHolder(bind.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when(viewType){

            TYPE_RADIO_STATION -> {
                val view = RadioItemBinding.inflate(
                    LayoutInflater.from(parent.context),  parent, false
                )

                return StationViewHolder(view)

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


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        if(item is StationWithDateModel.DateSeparator)
            (holder as DateSeparatorViewHolder).apply {
                if(currentDate == item.date){
                    bind.tvDate.text = "Today"
                } else {
                    bind.tvDate.text = item.date
                }
            }

        else if(item is StationWithDateModel.DateSeparatorEnclosing)
            (holder as DateSeparatorEnclosingViewHolder).apply {
                if(currentDate == item.date){
                    bind.tvDate.text = "Today"
                } else {
                    bind.tvDate.text = item.date
                }
            }

        else if (item is StationWithDateModel.Station){
            (holder as StationViewHolder).bind.apply {

                tvPrimary.text = item.radioStation.name
                tvSecondary.text = item.radioStation.country
                glide
                    .load(item.radioStation.favicon)
                    .placeholder(R.drawable.ic_radio_default)
                    .transition(withCrossFade(glideFactory))
                    .into(ivItemImage)

                ivItemImage.setOnClickListener {

                    onItemClickListener?.let { click ->

                        click(item.radioStation)

                        if(item.radioStation.stationuuid == currentRadioStationID){

                            if(selectedAdapterPosition != holder.absoluteAdapterPosition){

                                previousItemHolder?.bind?.let {
                                    restoreState(it.radioItemRootLayout, it.tvPrimary, it.tvSecondary)
                                }

                            }

                        }
                        else {

                            selectedAdapterPosition = holder.absoluteAdapterPosition
                            currentRadioStationID = item.radioStation.stationuuid

                            previousItemHolder?.bind?.let {

                                restoreState(it.radioItemRootLayout, it.tvPrimary, it.tvSecondary)
                            }

                        }
                        selectedAdapterPosition = holder.absoluteAdapterPosition
                        previousItemHolder = holder
                    }
                }


                if(item.radioStation.stationuuid == currentRadioStationID){

                        previousItemHolder?.bind?.let {
                            restoreState(it.radioItemRootLayout, it.tvPrimary, it.tvSecondary)
                        }

                        previousItemHolder = holder
                        selectedAdapterPosition = holder.absoluteAdapterPosition
                        handleStationPlaybackState(radioItemRootLayout, tvPrimary, tvSecondary)

                } else

                    restoreState(radioItemRootLayout, tvPrimary, tvSecondary)

            }
        }
    }


    var defaultTextColor = 0
    var selectedTextColor = 0


    private fun restoreState(background : View, tvPrimary : TextView, tvSecondary : TextView){
        background.setBackgroundResource(R.color.main_background)
        tvPrimary.setTextColor(defaultTextColor)
        tvSecondary.setTextColor(defaultTextColor)

    }

    private fun handleStationPlaybackState(background : View, tvPrimary : TextView, tvSecondary : TextView){
        if(currentPlaybackState){
            background.setBackgroundResource(R.drawable.radio_selected_gradient)
            tvPrimary.setTextColor(selectedTextColor)
            tvSecondary.setTextColor(selectedTextColor)
        } else {
            background.setBackgroundResource(R.drawable.radio_selected_paused_gradient)
            tvPrimary.setTextColor(defaultTextColor)
            tvSecondary.setTextColor(defaultTextColor)
        }
    }


    fun updateStationPlaybackState(){
       previousItemHolder?.let{
           if(it.absoluteAdapterPosition == selectedAdapterPosition){
               handleStationPlaybackState(it.bind.radioItemRootLayout, it.bind.tvPrimary, it.bind.tvSecondary)
           }
       }
    }

    var currentRadioStationID = ""
    var currentPlaybackState = false
    private var selectedAdapterPosition = -2
    private var previousItemHolder : StationViewHolder? = null



    private var onItemClickListener : ((RadioStation) -> Unit)? = null

    fun setOnClickListener(listener : (RadioStation) -> Unit){
        onItemClickListener = listener
    }



//    if(item.radioStation.stationuuid == currentRadioStationID){
//
//        if(checkForInitialSelection) {
//            selectedAdapterPosition = holder.absoluteAdapterPosition
//            checkForInitialSelection = false
//        }
//
//        if(selectedAdapterPosition == holder.absoluteAdapterPosition
//            || previousItemHolder?.absoluteAdapterPosition == -1
//            && !isInitialShiftHandled || previousItemHolder?.absoluteAdapterPosition ==1
//        ) {
//            isInitialShiftHandled = true
//            handleStationPlaybackState(radioItemRootLayout)
//
//            previousItemHolder = holder
//        } else {
//            radioItemRootLayout.setBackgroundResource(R.color.main_background)
//        }
//    } else
//    radioItemRootLayout.setBackgroundResource(R.color.main_background)
//    Log.d("CHECKTAGS", previousItemHolder?.absoluteAdapterPosition.toString())



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
            return oldItem == newItem
        }
    }

    var currentDate : String = ""

}