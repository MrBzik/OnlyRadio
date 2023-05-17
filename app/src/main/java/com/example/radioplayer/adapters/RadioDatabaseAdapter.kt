package com.example.radioplayer.adapters

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.ItemRadioWithTextBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.animations.fadeOut
import com.example.radioplayer.ui.fragments.FavStationsFragment
import com.example.radioplayer.utils.RandomColors
import javax.inject.Inject

class RadioDatabaseAdapter @Inject constructor(
    private val glide : RequestManager
) : RecyclerView.Adapter<RadioDatabaseAdapter.RadioItemHolder>() {

    private val randColors = RandomColors()

    class RadioItemHolder (val bind : ItemRadioWithTextBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioItemHolder {
       val holder = RadioItemHolder(
            ItemRadioWithTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
       )

        holder.itemView.setOnLongClickListener {
            val station =  listOfStations[holder.absoluteAdapterPosition]
            val clipText = station.stationuuid
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData("STATION_ID", mimeTypes, item)
            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)
            FavStationsFragment.dragAndDropItemPos = holder.absoluteAdapterPosition
            FavStationsFragment.dragAndDropStation = station
            true
        }


        holder.itemView.setOnClickListener {
            val station = listOfStations[holder.absoluteAdapterPosition]
            onItemClickListener?.let { click ->
                click(station, holder.absoluteAdapterPosition)

                updateOnStationChange(station, holder, true)
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: RadioItemHolder, @SuppressLint("RecyclerView") position: Int) {
        val station = listOfStations[position]

        holder.bind.apply {

            tvPrimary.text = station.name
            tvPrimary.textSize = titleSize
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
                tvPlaceholder.setBackgroundColor(color)
                tvPlaceholder.alpha = alpha
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

                                tvPlaceholder.fadeOut(300, alpha, position){ pos ->
                                    if(pos != holder.bindingAdapterPosition) {
                                        tvPlaceholder.alpha = alpha
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
                    .into(ivItemImage)
            }
        }

        if(station.stationuuid == currentRadioStationId) {
            selectedAdapterPosition = holder.absoluteAdapterPosition
            previousItemHolder = holder
            handleStationPlaybackState(holder.bind)
        } else
            restoreState(holder.bind)
    }

    private var selectedAdapterPosition = -2

    var defaultTextColor = 0
    var selectedTextColor = 0
    var defaultSecondaryTextColor = 0
    var selectedSecondaryTextColor = 0

    var separatorDefault = 0

    var alpha = 0.1f
    var titleSize = 20f

    var currentRadioStationId : String? = null
    var currentPlaybackState = false
    private var previousItemHolder : RadioItemHolder? = null

    private fun restoreState(bind: ItemRadioWithTextBinding){
        bind.apply {
            radioItemRootLayout.setBackgroundResource(R.color.main_background)
            tvPrimary.setTextColor(defaultTextColor)

            tvSecondary.setTextColor(defaultSecondaryTextColor)

            viewBottomSeparator?.setBackgroundColor(separatorDefault)
        }
    }

    private fun handleStationPlaybackState(bind: ItemRadioWithTextBinding){

        if(currentPlaybackState){
            bind.apply {
                radioItemRootLayout.setBackgroundResource(R.drawable.radio_selected_gradient)
                tvPrimary.setTextColor(selectedTextColor)

                tvSecondary.setTextColor(selectedSecondaryTextColor)

                viewBottomSeparator?.setBackgroundResource(R.color.station_bottom_separator_active)
            }

        } else {
            bind.apply {
                radioItemRootLayout.setBackgroundResource(R.drawable.radio_unselected_gradient)
                tvPrimary.setTextColor(defaultTextColor)

                tvSecondary.setTextColor(defaultSecondaryTextColor)

                viewBottomSeparator?.setBackgroundResource(R.color.station_bottom_separator_selected)
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

    fun updateOnStationChange(station : RadioStation, holder : RadioItemHolder?,
                              isClicked : Boolean = false
    ){
        if(station.stationuuid != currentRadioStationId) {
            currentRadioStationId = station.stationuuid
            previousItemHolder?.bind?.let {
                restoreState(it)
            }
        }
        holder?.let {
            selectedAdapterPosition = holder.absoluteAdapterPosition
            previousItemHolder = holder
            if(!isClicked){
                handleStationPlaybackState(holder.bind)
            }
        } ?: kotlin.run {
            selectedAdapterPosition = -2
        }
    }


    private var onItemClickListener : ((RadioStation, Int) -> Unit)? = null

    fun setOnClickListener(listener : (RadioStation, Int) -> Unit){
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

