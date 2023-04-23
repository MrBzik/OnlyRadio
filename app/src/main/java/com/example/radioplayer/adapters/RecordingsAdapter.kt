package com.example.radioplayer.adapters

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.databinding.ItemRecordingWithSeekbarBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.animations.fadeOut
import com.example.radioplayer.utils.RandomColors
import com.example.radioplayer.utils.Utils
import org.w3c.dom.Text
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class RecordingsAdapter @Inject constructor(
    private val glide : RequestManager,
    private val glideFactory : DrawableCrossFadeFactory
) : RecyclerView.Adapter<RecordingsAdapter.RecordingItemHolder>() {

    private val randColors = RandomColors()

    class RecordingItemHolder (val bind : ItemRecordingWithSeekbarBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingItemHolder {
       val holder = RecordingItemHolder(
           ItemRecordingWithSeekbarBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
       )

        holder.itemView.setOnClickListener {

            val recording = differ.currentList[holder.absoluteAdapterPosition]

            onItemClickListener?.let { click ->
                click(recording)

                if(playingRecordingId != recording.id) {

                    previousSeekbar?.let{
                        it.setOnSeekBarChangeListener(null)
                        it.visibility = View.GONE
                    }
                    previousTvTime?.text = Utils.timerFormat(previousTvTimeValue)

                    holder.bind.apply {

                        seekBar.max = recording.durationMills.toInt()
                        seekBar.progress = 0
                        previousSeekbar = seekBar
                        previousTvTime = tvDuration
                        previousTvTimeValue = recording.durationMills

                        itemSeekbarHandler?.let { handler ->

                            handler(seekBar, tvDuration, true)
                        }
                    }
                }
            }
        }

        return holder
    }


    override fun onBindViewHolder(holder: RecordingItemHolder, @SuppressLint("RecyclerView") position: Int) {

        val recording = listOfRecordings[position]

        holder.bind.apply {

            tvPrimary.text = recording.name
            tvSecondary.text = Utils.convertLongToDate(recording.timeStamp)

            val color = randColors.getColor()
            tvPlaceholder.setBackgroundColor(color)
            tvPlaceholder.alpha = alpha


            if(recording.iconUri.isBlank()) {

                ivItemImage.visibility = View.GONE

            } else {

                glide
                    .load(recording.iconUri)
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


            if(recording.id == playingRecordingId){

                previousSeekbar = seekBar
                previousTvTime = tvDuration
                previousTvTimeValue = recording.durationMills

                seekBar.max = recording.durationMills.toInt()

                itemSeekbarHandler?.let { handler ->
                    handler(seekBar, tvDuration,false)
                }

            } else {
                seekBar.visibility = View.GONE
                tvDuration.text = Utils.timerFormat(recording.durationMills)
                }
            }
        }


    var alpha = 0.1f

    var playingRecordingId = ""

    var previousSeekbar : SeekBar? = null
    var previousTvTime : TextView? = null

    var previousTvTimeValue = 0L

    private var itemSeekbarHandler : ((seekBar : SeekBar, tvDuration : TextView, isNewItem : Boolean) -> Unit)? = null

    fun setItemSeekbarHandler (handler : (seekbar : SeekBar, tvDuration : TextView, isNewItem : Boolean) -> Unit){
        itemSeekbarHandler = handler
    }


    private var onItemClickListener : ((Recording) -> Unit)? = null

    fun setOnClickListener(listener : (Recording) -> Unit){
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
       return listOfRecordings.size
    }



    private val diffCallback = object : DiffUtil.ItemCallback<Recording>(){

        override fun areItemsTheSame(oldItem: Recording, newItem: Recording): Boolean {
           return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recording, newItem: Recording): Boolean {
            return oldItem.id == newItem.id && oldItem.name == newItem.name
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var listOfRecordings : List<Recording>
    get() = differ.currentList
    set(value) = differ.submitList(value)


}

