package com.example.radioplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.databinding.ItemRecordingBinding
import com.example.radioplayer.databinding.ItemRecordingWithSeekbarBinding
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
            tvDuration.text = recording.duration
            glide
                .load(recording.iconUri)
                .placeholder(R.drawable.ic_radio_default)
                .transition(withCrossFade(glideFactory))
                .into(ivItemImage)
        }



            holder.bind.ivItemImage.setOnClickListener {

            onItemClickListener?.let { click ->
                click(recording)

               holder.bind.seekBar.visibility = View.VISIBLE

            }
        }

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
            return oldItem.id == newItem.id
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var listOfRecordings : List<Recording>
    get() = differ.currentList
    set(value) = differ.submitList(value)


}

