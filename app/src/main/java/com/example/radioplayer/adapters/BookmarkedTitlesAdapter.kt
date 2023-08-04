package com.example.radioplayer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.radioplayer.data.local.entities.BookmarkedTitle
import com.example.radioplayer.databinding.ItemBookmarkedTitleBinding
import com.example.radioplayer.extensions.loadImage
import com.example.radioplayer.ui.animations.AdapterFadeAnim.adapterItemFadeIn
import com.example.radioplayer.utils.RandomColors
import com.example.radioplayer.utils.Utils
import javax.inject.Inject

class BookmarkedTitlesAdapter @Inject constructor(
    private val glide : RequestManager
) : RecyclerView.Adapter<BookmarkedTitlesAdapter.TitleViewHolder>() {

    private val randColors = RandomColors()

    class TitleViewHolder (val bind : ItemBookmarkedTitleBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TitleViewHolder {
       val holder = TitleViewHolder(
           ItemBookmarkedTitleBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
       )

        holder.itemView.setOnClickListener {
            val title = listOfTitles[holder.absoluteAdapterPosition]
            onItemClickListener?.let { click ->
                click(title)

            }
        }


        return holder
    }

    override fun onBindViewHolder(holder: TitleViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val title = listOfTitles[position]

        holder.bind.apply {

            tvPrimary.text = title.title
            tvPrimary.textSize = titleSize

            tvSecondary.text = title.stationName
            tvTime.text = Utils.convertLongToDate(title.timeStamp, false)


            title.title.let { titleName ->
                var char = 'X'

                for (l in titleName.indices) {
                    if (titleName[l].isLetter()) {
                        char = titleName[l]
                        break
                    }
                }

                val color = randColors.getColor()

                tvPlaceholder.text = char.toString().uppercase()
                tvPlaceholder.setBackgroundColor(color)
                tvPlaceholder.alpha = alpha
            }


            if (title.stationIconUri.isBlank()) {

                ivItemImage.visibility = View.GONE

            } else {

                glide.loadImage(
                    uri = title.stationIconUri,
                    tvPlaceholder = tvPlaceholder,
                    ivItemImage = ivItemImage,
                    alpha = alpha,
                    position = position
                ){
                    holder.bindingAdapterPosition
                }
            }
        }

            adapterItemFadeIn(holder.itemView)

    }


    var alpha = 0.1f
    var titleSize = 20f



    override fun getItemCount(): Int {
       return listOfTitles.size
    }


    private var onItemClickListener : ((BookmarkedTitle) -> Unit)? = null

    fun setOnClickListener(listener : (BookmarkedTitle) -> Unit){
        onItemClickListener = listener
    }


    private val diffCallback = object : DiffUtil.ItemCallback<BookmarkedTitle>(){

        override fun areItemsTheSame(oldItem: BookmarkedTitle, newItem: BookmarkedTitle): Boolean {
           return oldItem.timeStamp == newItem.timeStamp
        }

        override fun areContentsTheSame(oldItem: BookmarkedTitle, newItem: BookmarkedTitle): Boolean {
            return oldItem.timeStamp == newItem.timeStamp
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var listOfTitles : List<BookmarkedTitle>
    get() = differ.currentList
    set(value) = differ.submitList(value)


}

