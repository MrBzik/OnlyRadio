package com.example.radioplayer.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.example.radioplayer.adapters.models.TitleWithDateModel
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Title
import com.example.radioplayer.databinding.ItemDateSeparatorBinding
import com.example.radioplayer.databinding.ItemDateSeparatorEnclosingBinding
import com.example.radioplayer.databinding.ItemRadioWithTextBinding
import com.example.radioplayer.databinding.ItemTitleBinding
import com.example.radioplayer.databinding.RadioItemBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.animations.fadeOut
import com.example.radioplayer.ui.fragments.HistoryFragment
import com.example.radioplayer.utils.RandomColors
import com.example.radioplayer.utils.Utils
import com.example.radioplayer.utils.Utils.convertLongToDate
import javax.inject.Inject


private const val TYPE_TITLE = 0
private const val TYPE_DATE_SEPARATOR = 1
private const val TYPE_DATE_SEPARATOR_ENCLOSING = 2


class TitleAdapter @Inject constructor(
    private val glide : RequestManager

) : PagingDataAdapter<TitleWithDateModel, RecyclerView.ViewHolder>(TitlesComparator) {

    private val randColors = RandomColors()

    class TitleViewHolder (val bind: ItemTitleBinding)
        : RecyclerView.ViewHolder(bind.root)

    class DateSeparatorViewHolder (val bind : ItemDateSeparatorBinding)
        : RecyclerView.ViewHolder(bind.root)

    class DateSeparatorEnclosingViewHolder (val bind : ItemDateSeparatorEnclosingBinding)
        : RecyclerView.ViewHolder(bind.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when(viewType){

            TYPE_TITLE -> {
                val holder = TitleViewHolder(
                    ItemTitleBinding.inflate(
                        LayoutInflater.from(parent.context),  parent, false
                    )
                )

                holder.itemView.setOnClickListener {

                    val item = getItem(holder.absoluteAdapterPosition) as TitleWithDateModel.TitleItem

                    onItemClickListener?.let { click ->

                        click(item.title)

                    }
                }


                holder.bind.tvBookmark.setOnClickListener {

                    val item = getItem(holder.absoluteAdapterPosition) as TitleWithDateModel.TitleItem

                    onBookmarkClickListener?.let { click ->
                        click(item.title)

//                        handleBookmarkImage(!item.title.isBookmarked, holder.bind.ivBookmark)
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
        val item = getItem(position)

        if(item is TitleWithDateModel.TitleDateSeparator)
            (holder as DateSeparatorViewHolder).apply {
                if(RadioService.currentDateString == item.date){
                    bind.tvDate.text = "Today"
                } else {
                    bind.tvDate.text = item.date
                }
            }

        else if(item is TitleWithDateModel.TitleDateSeparatorEnclosing)
            (holder as DateSeparatorEnclosingViewHolder).apply {
                if(RadioService.currentDateString == item.date){
                    bind.tvDate.text = "Today"
                } else {
                    bind.tvDate.text = item.date
                }
            }

        else if (item is TitleWithDateModel.TitleItem){
            (holder as TitleViewHolder).bind.apply {

                val title = item.title

//                handleBookmarkImage(title.isBookmarked, holder.bind.ivBookmark)

                tvPrimary.text = title.title
                tvPrimary.textSize = titleSize

                tvSecondary.text = title.stationName
                tvTime.text = convertLongToDate(title.timeStamp, true)


                title.title.let { titleName ->
                    var char = 'X'

                    for(l in titleName.indices){
                        if(titleName[l].isLetter()){
                            char = titleName[l]
                            break
                        }
                    }

                    val color = randColors.getColor()

                    tvPlaceholder.text = char.toString().uppercase()
                    tvPlaceholder.setBackgroundColor(color)
                    tvPlaceholder.alpha = alpha
                }


                if(title.stationIconUri.isBlank()) {

                    ivItemImage.visibility = View.GONE

                } else {

                    glide
                        .load(title.stationIconUri)
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
                        .apply(RequestOptions().override(50, 50))
                        .into(ivItemImage)
                }
            }
        }

        HistoryFragment.adapterAnimator.animateAppearance(holder.itemView)
    }


    var alpha = 0.1f
    var titleSize = 20f





    private var onItemClickListener : ((Title) -> Unit)? = null

    fun setOnClickListener(listener : (Title) -> Unit){
        onItemClickListener = listener
    }


    private var onBookmarkClickListener : ((Title) -> Unit)? = null

    fun onBookmarkClickListener(listener : (Title) -> Unit){
        onBookmarkClickListener = listener
    }

    override fun getItemViewType(position: Int): Int {

        return when(getItem(position)){
            is TitleWithDateModel.TitleItem -> TYPE_TITLE
            is TitleWithDateModel.TitleDateSeparator -> TYPE_DATE_SEPARATOR
            else -> TYPE_DATE_SEPARATOR_ENCLOSING
        }
    }


   object TitlesComparator : DiffUtil.ItemCallback<TitleWithDateModel>(){

        override fun areItemsTheSame(
            oldItem: TitleWithDateModel, newItem: TitleWithDateModel
        ): Boolean {

            val isSameTitle = oldItem is TitleWithDateModel.TitleItem
                    && newItem is TitleWithDateModel.TitleItem
                    && oldItem.title.timeStamp == newItem.title.timeStamp


            val isSameSeparator = oldItem is TitleWithDateModel.TitleDateSeparator
                    && newItem is TitleWithDateModel.TitleDateSeparator
                    && oldItem.date == newItem.date

            val isSameSeparatorEnclosing = oldItem is TitleWithDateModel.TitleDateSeparatorEnclosing
                    && newItem is TitleWithDateModel.TitleDateSeparatorEnclosing
                    && oldItem.date == newItem.date

            return isSameTitle || isSameSeparator || isSameSeparatorEnclosing


        }

        override fun areContentsTheSame(
            oldItem: TitleWithDateModel, newItem: TitleWithDateModel
        ): Boolean {

            val isSameTitle = oldItem is TitleWithDateModel.TitleItem
                    && newItem is TitleWithDateModel.TitleItem
                    && oldItem.title.timeStamp == newItem.title.timeStamp

            val isSameSeparator = oldItem is TitleWithDateModel.TitleDateSeparator
                    && newItem is TitleWithDateModel.TitleDateSeparator
                    && oldItem.date == newItem.date

            val isSameSeparatorEnclosing = oldItem is TitleWithDateModel.TitleDateSeparatorEnclosing
                    && newItem is TitleWithDateModel.TitleDateSeparatorEnclosing
                    && oldItem.date == newItem.date

            return isSameTitle || isSameSeparator || isSameSeparatorEnclosing

        }
    }

}