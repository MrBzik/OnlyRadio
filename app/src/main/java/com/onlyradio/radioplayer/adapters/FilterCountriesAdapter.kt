package com.onlyradio.radioplayer.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.onlyradio.radioplayer.adapters.models.CountryWithRegion
import com.onlyradio.radioplayer.databinding.ItemCountryBinding
import com.onlyradio.radioplayer.databinding.ItemGenreBinding

const val VIEW_TYPE_COUNTRY = 0
const val VIEW_TYPE_REGION = 1

class FilterCountriesAdapter(): ListAdapter<CountryWithRegion, RecyclerView.ViewHolder>(DIFF_CALLBACK),
    Filterable {

    var originalList: List<CountryWithRegion> = currentList.toList()

    class CountryHolder (val bind : ItemCountryBinding) : RecyclerView.ViewHolder(bind.root)

    class RegionHolder (val bind : ItemGenreBinding) : RecyclerView.ViewHolder(bind.root)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){
            is CountryWithRegion.Country -> VIEW_TYPE_COUNTRY
            is CountryWithRegion.Region -> VIEW_TYPE_REGION
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if(viewType == VIEW_TYPE_COUNTRY) {
            val holder = CountryHolder (
                ItemCountryBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            holder.itemView.setOnClickListener {
                if(holder.bindingAdapterPosition >= 0){
                    val item = getItem(holder.bindingAdapterPosition) as CountryWithRegion.Country
                    onItemClickListener?.let { click ->
                        click(item, holder.bindingAdapterPosition)
                    }
                }
            }
            return holder

        } else {
            val holder = RegionHolder(
                ItemGenreBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            holder.itemView.setOnClickListener {
                if(holder.bindingAdapterPosition >= 0){
                    val item = getItem(holder.bindingAdapterPosition) as CountryWithRegion.Region
                    handleGenreTextStyle(!item.isOpened, holder.bind.tvText)
                    onItemClickListener?.let { click ->
                        click(item, holder.bindingAdapterPosition)
                    }
                }
            }
            return holder
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = getItem(position)

        if(item is CountryWithRegion.Country){

            (holder as CountryHolder).bind.apply {

                tvCountryName.text = item.countryName

                tvStationsCount.text = item.stationsCount.toString()

                Glide.with(ivCountryFlag)
                    .load(
                        Uri.parse(
                            "file:///android_asset/flags/${item.countryCode.lowercase()}.png"))
                    .into(ivCountryFlag)

            }
        } else if(item is CountryWithRegion.Region){
            (holder as RegionHolder).apply {
                bind.tvText.text = item.region
                handleGenreTextStyle(item.isOpened, bind.tvText)
            }
        }
    }


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


    private var onItemClickListener : ((countryItem : CountryWithRegion, position: Int) -> Unit)? = null

    fun setOnCountryClickListener(listener : (countryItem : CountryWithRegion, position: Int) -> Unit){
        onItemClickListener = listener
    }


    override fun getFilter(): Filter {

            return object : Filter() {
                override fun performFiltering(charSequence: CharSequence): FilterResults {

                    return FilterResults().apply {
                        values = if (charSequence.isEmpty())
                            originalList
                        else {
                            originalList.filter { country ->

                                country is CountryWithRegion.Country &&

                                country.countryName.contains(charSequence, ignoreCase = true)
                            }
                        }
                    }
                }

                override fun publishResults(charSequence: CharSequence, filterResults: Filter.FilterResults) {
                    submitList(filterResults.values as List<CountryWithRegion>, true)

                }
            }
    }

    override fun submitList(list: List<CountryWithRegion>?) {
        submitList(list, false)
    }


    private fun submitList(list: List<CountryWithRegion>?, filtered: Boolean) {
        if (!filtered)
            originalList = list ?: listOf()

        super.submitList(list)
    }


    companion object {
        val DIFF_CALLBACK= object: DiffUtil.ItemCallback<CountryWithRegion>() {

            override fun areItemsTheSame(oldItem: CountryWithRegion, newItem: CountryWithRegion): Boolean {
                return oldItem is CountryWithRegion.Country && newItem is CountryWithRegion.Country
                        && oldItem.countryCode == newItem.countryCode ||
                        oldItem is CountryWithRegion.Region && newItem is CountryWithRegion.Region
                        && oldItem.region == newItem.region

            }

            override fun areContentsTheSame(oldItem: CountryWithRegion, newItem: CountryWithRegion): Boolean {
                return oldItem is CountryWithRegion.Country && newItem is CountryWithRegion.Country
                        && oldItem.countryCode == newItem.countryCode ||
                        oldItem is CountryWithRegion.Region && newItem is CountryWithRegion.Region
                        && oldItem.region == newItem.region

            }
        }
    }
}