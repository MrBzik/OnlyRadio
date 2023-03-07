package com.example.radioplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.radioplayer.databinding.ItemTextBinding

class FilterTagsAdapter(): ListAdapter<String, FilterTagsAdapter.TagHolder>(DIFF_CALLBACK),
    Filterable {

    var originalList: List<String> = currentList.toList()

    class TagHolder (val bind : ItemTextBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagHolder {

        return TagHolder(
            ItemTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: TagHolder, position: Int) {

        val item = getItem(position)

        holder.bind.tvText.text = item

        holder.itemView.setOnClickListener {

            onItemClickListener?.let { click ->
                click(item)
            }

        }

    }

    private var onItemClickListener : ((String) -> Unit)? = null

    fun setOnTagClickListener(listener : (String) -> Unit){
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
                                tag.contains(charSequence, ignoreCase = true) &&
                                !tag.contains("---")
                            }
                        }
                    }
                }

                override fun publishResults(charSequence: CharSequence, filterResults: Filter.FilterResults) {
                    submitList(filterResults.values as List<String>, true)

                }
            }
    }

    override fun submitList(list: List<String>?) {
        submitList(list, false)
    }


    private fun submitList(list: List<String>?, filtered: Boolean) {
        if (!filtered)
            originalList = list ?: listOf()

        super.submitList(list)
    }


    companion object {

        val DIFF_CALLBACK= object: DiffUtil.ItemCallback<String>() {

            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem

            }
        }
    }
}