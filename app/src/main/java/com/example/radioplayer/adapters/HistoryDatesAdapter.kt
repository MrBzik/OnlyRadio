package com.example.radioplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.HistoryDate
import java.text.DateFormat
import java.util.*


class HistoryDatesAdapter(
    private val datesList : List<HistoryDate>,
    private val requireContext : Context ) : BaseAdapter(){

    override fun getCount(): Int {
        return datesList.size +1
    }

    override fun getItem(position: Int): Any {

        return if(position == 0){
            HistoryDate("History: All", 0)
        } else {
            datesList[position -1]
        }


    }



    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

       val view = LayoutInflater.from(requireContext).inflate(R.layout.item_spinner_date, parent, false)

       val textView = view.findViewById<TextView>(R.id.tvSpinnerDate)

       if(position != 0){

           val date = Date(datesList[position -1].time)

           val res = format.format(date)

           textView.text = res

           if(position == selectedItemPosition) textView.setTextColor(selectedColor)

       } else {

           textView.text = "History: All"
            if(selectedItemPosition <=0){
                textView.setTextColor(selectedColor)
            }
       }

        return view
    }

    private val format = DateFormat.getDateInstance()

     var selectedItemPosition = 0
     var selectedColor = 0

}