package com.example.radioplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.radioplayer.R
import com.example.radioplayer.data.remote.pixabay.Hit
import com.example.radioplayer.databinding.ItemPixabayImageBinding
import javax.inject.Inject

class PagingPixabayAdapter @Inject constructor(
    private val glide : RequestManager

) : PagingDataAdapter<Hit, PagingPixabayAdapter.ImageHolder>(ImagesComparator) {

    class ImageHolder (itemView : View) : RecyclerView.ViewHolder(itemView)  {

        var bind : ItemPixabayImageBinding
        init {
            bind = ItemPixabayImageBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
       return ImageHolder(
           LayoutInflater.from(parent.context).inflate(R.layout.item_pixabay_image, parent, false)
       )

    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {

        val currentImage = getItem(position)!!



        glide.load(currentImage.previewURL)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.pixabay_logo_square)
            .into(holder.bind.ivPixabayImage)

        holder.itemView.setOnClickListener {

            onItemClickListener?.let { click ->

                click(currentImage)
            }
        }


    }

    private var onItemClickListener : ((Hit) -> Unit)? = null

    fun setOnClickListener(listener : (Hit) -> Unit){
        onItemClickListener = listener
    }



    object ImagesComparator : DiffUtil.ItemCallback<Hit>(){

        override fun areItemsTheSame(oldItem: Hit, newItem: Hit): Boolean {
           return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Hit, newItem: Hit): Boolean {
            return oldItem == newItem
        }
    }

    override fun onViewRecycled(holder: ImageHolder) {
        try {
            holder.bind.ivPixabayImage.apply {
                glide.clear(this)
            }
        } catch (e : Exception) {

        }

        super.onViewRecycled(holder)
    }

}