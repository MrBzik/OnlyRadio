package com.example.radioplayer.adapters

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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.databinding.ItemRecordingWithSeekbarBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.utils.Utils
import org.w3c.dom.Text
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class RecordingsAdapter @Inject constructor(
    private val glide : RequestManager,
    private val glideFactory : DrawableCrossFadeFactory
) : RecyclerView.Adapter<RecordingsAdapter.RecordingItemHolder>() {

    class RecordingItemHolder (val bind : ItemRecordingWithSeekbarBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingItemHolder {
       return RecordingItemHolder(
           ItemRecordingWithSeekbarBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
       )
    }

    private fun convertLongToDate(time : Long) : String{
        val date = Date(time)
        val format = DateFormat.getDateTimeInstance()
        return format.format(date)
    }

    override fun onBindViewHolder(holder: RecordingItemHolder, position: Int) {

        val recording = listOfRecordings[position]

        holder.bind.apply {

            tvPrimary.text = recording.name
            tvSecondary.text = convertLongToDate(recording.timeStamp)

            glide
                .load(recording.iconUri)
                .placeholder(R.drawable.ic_radio_default)
                .transition(withCrossFade(glideFactory))
                .into(ivItemImage)


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

            root.setOnClickListener {

                onItemClickListener?.let { click ->
                    click(recording, position)

                    if(playingRecordingId == recording.id) {/*DO NOTHING*/}
                    else {

                        previousSeekbar?.let{
                            it.setOnSeekBarChangeListener(null)
                            it.visibility = View.GONE
                        }
                        previousTvTime?.text = Utils.timerFormat(previousTvTimeValue)

                        seekBar.max = recording.durationMills.toInt()
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
        }


    var playingRecordingId = ""

    var previousSeekbar : SeekBar? = null
    var previousTvTime : TextView? = null

    var previousTvTimeValue = 0L

    private var itemSeekbarHandler : ((seekBar : SeekBar, tvDuration : TextView, isNewItem : Boolean) -> Unit)? = null

    fun setItemSeekbarHandler (handler : (seekbar : SeekBar, tvDuration : TextView, isNewItem : Boolean) -> Unit){
        itemSeekbarHandler = handler
    }


    private var onItemClickListener : ((Recording, position : Int) -> Unit)? = null

    fun setOnClickListener(listener : (Recording, position : Int) -> Unit){
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
            return oldItem.id == newItem.id
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var listOfRecordings : List<Recording>
    get() = differ.currentList
    set(value) = differ.submitList(value)


}

