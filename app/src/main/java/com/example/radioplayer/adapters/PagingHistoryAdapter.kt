package com.example.radioplayer.adapters

import android.annotation.SuppressLint
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
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.radioplayer.R
import com.example.radioplayer.adapters.models.StationWithDateModel
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.ItemDateSeparatorBinding
import com.example.radioplayer.databinding.ItemDateSeparatorEnclosingBinding
import com.example.radioplayer.databinding.ItemRadioWithTextBinding
import com.example.radioplayer.databinding.RadioItemBinding
import com.example.radioplayer.ui.animations.fadeOut
import com.example.radioplayer.utils.RandomColors
import javax.inject.Inject


private const val TYPE_RADIO_STATION = 0
private const val TYPE_DATE_SEPARATOR = 1
private const val TYPE_DATE_SEPARATOR_ENCLOSING = 2


class PagingHistoryAdapter @Inject constructor(
    private val glide : RequestManager

) : PagingDataAdapter<StationWithDateModel, RecyclerView.ViewHolder>(StationsComparator) {

    private val randColors = RandomColors()

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

                    onItemClickListener?.let { click ->

                        click(item.radioStation)
                    }

                    if(item.radioStation.stationuuid == currentRadioStationID){

                        if(selectedAdapterPosition != holder.absoluteAdapterPosition){

                            previousItemHolder?.bind?.let {
                                restoreState(it)
                            }
                        }
                    }
                    else {

                        selectedAdapterPosition = holder.absoluteAdapterPosition
                        currentRadioStationID = item.radioStation.stationuuid

                        previousItemHolder?.bind?.let {

                            restoreState(it)
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

                val station = item.radioStation

                tvPrimary.text = station.name
                tvSecondary.apply {
                    if(station.country?.isNotBlank() == true){
                        visibility = View.VISIBLE
                        text = station.country
                    }

                    else visibility = View.GONE
                }

                station.name?.let { name ->
                    var char = 'X'

                    for(l in name.indices){
                        if(name[l].isLetter()){
                            char = name[l]
                            break
                        }
                    }

                    val color = randColors.getColor()

                    tvPlaceholder.text = char.toString().uppercase()
                    tvPlaceholder.setTextColor(color)
                    tvPlaceholder.alpha = 0.6f
                }


                if(station.favicon.isNullOrBlank()) {

                    ivItemImage.visibility = View.GONE

                } else {

                    glide
                        .load(station.favicon)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {

                                return true
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {

                                if(dataSource?.name == "REMOTE"){

                                    tvPlaceholder.fadeOut(300, 0.6f, position){ pos ->
                                        if(pos != holder.bindingAdapterPosition) {
                                            tvPlaceholder.alpha = 0.6f
                                        }
                                    }

                                }
                                else {

                                    tvPlaceholder.alpha = 0f
                                }
                                ivItemImage.visibility = View.VISIBLE
                                return false
                            }
                        })
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(RequestOptions().override(65, 65))
                        .into(ivItemImage)
                }


                if(station.stationuuid == currentRadioStationID){

                        previousItemHolder?.bind?.let {
                            restoreState(it)
                        }

                        previousItemHolder = holder
                        selectedAdapterPosition = holder.absoluteAdapterPosition
                        handleStationPlaybackState(this)

                } else

                    restoreState(this)

            }
        }
    }

    var defaultTextColor = 0
    var selectedTextColor = 0


    private fun restoreState(bind: ItemRadioWithTextBinding){
        bind.apply {
            radioItemRootLayout.setBackgroundResource(R.color.main_background)
            tvPrimary.setTextColor(defaultTextColor)
            tvPrimary.alpha = 0.7f
            tvSecondary.setTextColor(defaultTextColor)
        }
    }

    private fun handleStationPlaybackState(bind: ItemRadioWithTextBinding){

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
       previousItemHolder?.let{
           if(it.absoluteAdapterPosition == selectedAdapterPosition){
               handleStationPlaybackState(it.bind)
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

    var currentDate : String = ""

}