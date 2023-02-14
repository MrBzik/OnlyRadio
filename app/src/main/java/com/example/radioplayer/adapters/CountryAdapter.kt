package com.example.radioplayer.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.DifferCallback
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.ItemCountryBinding
import com.example.radioplayer.utils.Country


class CountryAdapter (
) : RecyclerView.Adapter<CountryAdapter.CountryHolder>() {



    class CountryHolder (val bind : ItemCountryBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryHolder {
       return CountryHolder(
           ItemCountryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
       )
    }

    override fun onBindViewHolder(holder: CountryHolder, position: Int) {


        holder.bind.apply {

            tvCountryName.text = differ.currentList[position].countryName

            Glide.with(ivCountryFlag)
                .load(Uri.parse(
                    "file:///android_asset/flags/${differ.currentList[position].countryCode.lowercase()}.png"))
                .into(ivCountryFlag)

            root.setOnClickListener {
                onItemClickListener?.let { click ->

                    click(differ.currentList[position].countryCode)

                }
            }
        }
    }

    private var onItemClickListener : ((String) -> Unit)? = null

    fun setOnCountryClickListener(listener : (String) -> Unit){
        onItemClickListener = listener
    }


    override fun getItemCount(): Int {
       return differ.currentList.size
    }


    private val diffCallback = object : DiffUtil.ItemCallback<Country>(){

        override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem.countryCode == newItem.countryCode
        }

        override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem.countryCode == newItem.countryCode
        }
    }

     val differ = AsyncListDiffer(this, diffCallback)



}

