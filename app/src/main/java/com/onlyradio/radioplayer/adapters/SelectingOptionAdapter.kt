package com.onlyradio.radioplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.databinding.ItemTextBinding

class SelectingOptionAdapter(var listOfOptions : List<String>) :
    RecyclerView.Adapter<SelectingOptionAdapter.OptionHolder>() {

    class OptionHolder (val bind : ItemTextBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionHolder {
        return OptionHolder(
                ItemTextBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: OptionHolder, position: Int) {

        holder.bind.tvText.apply {
            text = listOfOptions[position]

            if(position == currentOption){
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.option_selected, 0)
                previousSelected = this
            } else {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            }
        }

        holder.itemView.setOnClickListener {

            onItemClickListener?.let { click ->

                click(position)
            }

            if(position != currentOption) {
                currentOption = position
                previousSelected?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
               holder.bind.tvText.apply {
                   previousSelected = this
                   setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.option_selected, 0)
               }
            }
        }
    }

    private var previousSelected : TextView? = null

    var currentOption = 3

    private var onItemClickListener : ((newOption : Int) -> Unit)? = null

    fun setOnItemClickListener(listener : (newOption : Int) -> Unit){
        onItemClickListener = listener
    }


    override fun getItemCount(): Int {
        return listOfOptions.size
    }
}