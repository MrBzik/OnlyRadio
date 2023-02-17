package com.example.radioplayer.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.radioplayer.databinding.ItemCountryBinding
import com.example.radioplayer.utils.Country

class FilterCountriesAdapter(): ListAdapter<Country, FilterCountriesAdapter.CountryHolder>(DIFF_CALLBACK),
    Filterable {

    var originalList: List<Country> = currentList.toList()

    class CountryHolder (val bind : ItemCountryBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryHolder {

        return CountryHolder(
            ItemCountryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CountryHolder, position: Int) {

        val item = getItem(position)

        holder.bind.apply {

            tvCountryName.text = item.countryName

            Glide.with(ivCountryFlag)
                .load(
                    Uri.parse(
                    "file:///android_asset/flags/${item.countryCode.lowercase()}.png"))
                .into(ivCountryFlag)

            root.setOnClickListener {
                onItemClickListener?.let { click ->

                    click(item.countryCode)

                }
            }
        }
    }

    private var onItemClickListener : ((String) -> Unit)? = null

    fun setOnCountryClickListener(listener : (String) -> Unit){
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
                                country.countryName.contains(charSequence, ignoreCase = true)
                            }
                        }
                    }
                }

                override fun publishResults(charSequence: CharSequence, filterResults: Filter.FilterResults) {
                    submitList(filterResults.values as List<Country>, true)

                }
            }
    }

    override fun submitList(list: List<Country>?) {
        submitList(list, false)
    }


    private fun submitList(list: List<Country>?, filtered: Boolean) {
        if (!filtered)
            originalList = list ?: listOf()

        super.submitList(list)
    }


    companion object {
        val DIFF_CALLBACK= object: DiffUtil.ItemCallback<Country>() {

            override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
                return oldItem.countryCode == newItem.countryCode
            }

            override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
                return oldItem.countryCode == newItem.countryCode

            }
        }
    }
}