package com.example.radioplayer.adapters

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible

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
import com.example.radioplayer.databinding.ItemViewPagerStationBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.animations.fadeOut
import com.example.radioplayer.utils.RandomColors
import javax.inject.Inject

class ViewPagerStationsAdapter constructor(
    private val glide : RequestManager,
    private val homePageClick : (String) -> Unit
) : RecyclerView.Adapter<ViewPagerStationsAdapter.StationDetailsHolder>() {

    private val randColors = RandomColors()

    class StationDetailsHolder (val bind : ItemViewPagerStationBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationDetailsHolder {
        return StationDetailsHolder(
           ItemViewPagerStationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
       )
    }

    override fun onBindViewHolder(holder: StationDetailsHolder, @SuppressLint("RecyclerView") position: Int) {
        val station = listOfStations[position]

        holder.bind.apply {

            tvName.text = station.name

            if(station.favicon.isNullOrBlank()){

                ivIcon.visibility = View.INVISIBLE
                setTvPlaceHolderLetter(station.name?: "", this)

            } else {
                ivIcon.visibility = View.VISIBLE

                glide
                    .load(station.favicon)
                    .listener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {

                            ivIcon.visibility = View.INVISIBLE
                            setTvPlaceHolderLetter(station.name ?: "", this@apply)
                            return true
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            tvPlaceholder.alpha = 0f
                            return false
                        }
                    })
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivIcon)
            }


            if(!station.language.isNullOrBlank()){
                tvLanguage.isVisible = true
                val languages = station.language.replace(",", ", ")
                tvLanguage.text = "Languages : $languages"
            }
            if(!station.tags.isNullOrBlank()){
                tvTags.isVisible = true
                val tags = station.tags.replace(",", ", ")
                tvTags.text = "Tags : $tags"
            }


            tvHomePage?.setOnClickListener {

                if(!station.homepage.isNullOrBlank()) {

                    homePageClick(station.homepage)

                }
            } ?: kotlin.run {
                btnHomePage?.setOnClickListener {

                    if(!station.homepage.isNullOrBlank()) {

                        homePageClick(station.homepage)

                    }
                }
            }
        }
    }





    private fun setTvPlaceHolderLetter(name : String, bind : ItemViewPagerStationBinding){

        val color = randColors.getColor()

        var char = 'X'

        for(l in name.indices){
            if(name[l].isLetter()){
                char = name[l]
                break
            }
        }

        bind.tvPlaceholder.apply {
            text = char.toString().uppercase()
            setTextColor(color)
            alpha = 0.6f
        }
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

