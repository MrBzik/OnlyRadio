package com.onlyradio.radioplayer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.adapters.models.TitleWithDateModel
import com.onlyradio.radioplayer.data.local.entities.Title
import com.onlyradio.radioplayer.databinding.ItemDateSeparatorBinding
import com.onlyradio.radioplayer.databinding.ItemDateSeparatorEnclosingBinding
import com.onlyradio.radioplayer.databinding.ItemTitleBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.extensions.loadImage
import com.onlyradio.radioplayer.ui.animations.AdapterFadeAnim.adapterItemFadeIn
import com.onlyradio.radioplayer.utils.RandomColors
import com.onlyradio.radioplayer.utils.Utils
import com.onlyradio.radioplayer.utils.Utils.convertLongToDate
import java.text.DateFormat
import javax.inject.Inject


private const val TYPE_TITLE = 0
private const val TYPE_DATE_SEPARATOR = 1
private const val TYPE_DATE_SEPARATOR_ENCLOSING = 2


class TitleAdapter constructor(
    private val glide : RequestManager,
    private val dateToday : String

) : PagingDataAdapter<TitleWithDateModel, RecyclerView.ViewHolder>(TitlesComparator) {

    private val randColors = RandomColors()

    private val currentDate = Utils.convertLongToOnlyDate(System.currentTimeMillis(), DateFormat.LONG)
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

                    if(holder.bindingAdapterPosition >= 0){
                        val item = getItem(holder.bindingAdapterPosition) as TitleWithDateModel.TitleItem

                        onItemClickListener?.let { click ->

                            click(item.title)

                        }
                    }
                }


                holder.bind.tvBookmark.setOnClickListener {
                    if (holder.bindingAdapterPosition >= 0){

                        val item = getItem(holder.bindingAdapterPosition) as TitleWithDateModel.TitleItem
                        onBookmarkClickListener?.let { click ->
                            click(item.title)
                        }
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
                if(currentDate == item.date){
                    bind.tvDate.text = dateToday
                } else {
                    bind.tvDate.text = item.date
                }
            }

        else if(item is TitleWithDateModel.TitleDateSeparatorEnclosing)
            (holder as DateSeparatorEnclosingViewHolder).apply {
                if(currentDate == item.date){
                    bind.tvDate.text = dateToday
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
        }

            adapterItemFadeIn(holder.itemView)
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