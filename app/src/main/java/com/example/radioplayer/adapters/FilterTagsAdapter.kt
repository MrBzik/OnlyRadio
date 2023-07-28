package com.example.radioplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.radioplayer.adapters.models.TagWithGenre
import com.example.radioplayer.databinding.ItemGenreBinding
import com.example.radioplayer.databinding.ItemTagBinding
import com.example.radioplayer.databinding.ItemTextBinding


const val VIEW_TYPE_TAG = 0
const val VIEW_TYPE_GENRE = 1

class FilterTagsAdapter(): ListAdapter<TagWithGenre, RecyclerView.ViewHolder>(DIFF_CALLBACK),
    Filterable {

    var originalList: List<TagWithGenre> = currentList.toList()

    class TagHolder (val bind : ItemTagBinding) : RecyclerView.ViewHolder(bind.root)

    class GenreHolder (val bind : ItemGenreBinding) : RecyclerView.ViewHolder(bind.root)


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){
            is TagWithGenre.Tag -> VIEW_TYPE_TAG
            is TagWithGenre.Genre -> VIEW_TYPE_GENRE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if(viewType == VIEW_TYPE_TAG) {
            val holder = TagHolder(
                ItemTagBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            holder.itemView.setOnClickListener {
                val item = getItem(holder.absoluteAdapterPosition) as TagWithGenre.Tag
                onItemClickListener?.let { click ->
                    click(item, holder.absoluteAdapterPosition)
                }
            }
            return holder

        } else {
           val holder = GenreHolder(
                ItemGenreBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            holder.itemView.setOnClickListener {
                val item = getItem(holder.absoluteAdapterPosition) as TagWithGenre.Genre
                handleGenreTextStyle(!item.isOpened, holder.bind.tvText)
                onItemClickListener?.let { click ->
                    click(item, holder.absoluteAdapterPosition)
                }
            }
            return holder
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = getItem(position)

        if(item is TagWithGenre.Tag){
            (holder as TagHolder).apply {
                bind.tvTagText.text = item.tag

                if(isExactMatch){
                    bind.tvStationsCount.text = item.stationCountExact.toString()
                } else{
                    bind.tvStationsCount.text = item.stationCount.toString()
                }



            }
        } else if(item is TagWithGenre.Genre) {
            (holder as GenreHolder).apply {
                bind.tvText.text = item.genre
                handleGenreTextStyle(item.isOpened, bind.tvText)
            }
        }

    }

    var isExactMatch = false

    var defaultTextColor = 0
    var selectedTextColor = 0
    var openingDrawable = 0
    var closingDrawable = 0


    private fun handleGenreTextStyle(isOpened : Boolean, textView : TextView){

        if(isOpened){
            textView.apply {

                textView.setTextColor(selectedTextColor)
                setCompoundDrawablesWithIntrinsicBounds(closingDrawable, 0, closingDrawable, 0)

            }


        } else {
            textView.apply {

                setTextColor(defaultTextColor)
                setCompoundDrawablesWithIntrinsicBounds(openingDrawable, 0, openingDrawable, 0)
            }

        }

    }

    private var onItemClickListener : ((tag : TagWithGenre, position : Int) -> Unit)? = null

    fun setOnTagClickListener(listener : (tag : TagWithGenre, position : Int) -> Unit){
        onItemClickListener = listener
    }


    override fun getFilter(): Filter {

            return object : Filter() {
                override fun performFiltering(charSequence: CharSequence): FilterResults {

                    return FilterResults().apply {
                        values = if (charSequence.isEmpty())
                            originalList
                        else {
                            originalList.filter { tag ->

                                tag is TagWithGenre.Tag &&
                                tag.tag.contains(charSequence, ignoreCase = true)
                            }
                        }
                    }
                }

                override fun publishResults(charSequence: CharSequence, filterResults: Filter.FilterResults) {
                    submitList(filterResults.values as List<TagWithGenre>, true)

                }
            }
    }

    override fun submitList(list: List<TagWithGenre>?) {
        submitList(list, false)
    }


    private fun submitList(list: List<TagWithGenre>?, filtered: Boolean) {
        if (!filtered)
            originalList = list ?: listOf()

        super.submitList(list?.toMutableList())
    }


    companion object {

        val DIFF_CALLBACK= object: DiffUtil.ItemCallback<TagWithGenre>() {

            override fun areItemsTheSame(oldItem: TagWithGenre, newItem: TagWithGenre): Boolean {
                return oldItem is TagWithGenre.Tag && newItem is TagWithGenre.Tag
                        && oldItem.tag == newItem.tag ||
                       oldItem is TagWithGenre.Genre && newItem is TagWithGenre.Genre
                        && oldItem.genre == newItem.genre
            }

            override fun areContentsTheSame(oldItem: TagWithGenre, newItem: TagWithGenre): Boolean {
                return oldItem is TagWithGenre.Tag && newItem is TagWithGenre.Tag
                        && oldItem.tag == newItem.tag ||
                        oldItem is TagWithGenre.Genre && newItem is TagWithGenre.Genre
                        && oldItem.genre == newItem.genre

            }
        }
    }
}