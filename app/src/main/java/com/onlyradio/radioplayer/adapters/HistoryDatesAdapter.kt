package com.onlyradio.radioplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.data.local.entities.HistoryDate
import com.onlyradio.radioplayer.utils.Utils
import java.text.DateFormat
import java.util.Calendar
import java.util.Date


class HistoryDatesAdapter(
    var datesList : List<HistoryDate>,
    private val requireContext : Context ) : BaseAdapter(){

    override fun getCount(): Int {
        return datesList.size
    }

    override fun getItem(position: Int): Any {

        return datesList[position]



//        return if(position == 0){
//            HistoryDate("History: All", 0)
//        } else {
//            datesList[position -1]
//        }



    }



    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

       val view = LayoutInflater.from(requireContext).inflate(R.layout.item_spinner_date, parent, false)

       val textView = view.findViewById<TextView>(R.id.tvSpinnerDate)

//        val date = Date(datesList[position].time)


       if(position != 0){

//           calendar.time = date
           val dateString = Utils.convertLongToOnlyDate(datesList[position].time, DateFormat.MEDIUM)

           textView.text = dateString

           if(position == selectedItemPosition) textView.setTextColor(selectedColor)

       } else {

           textView.text = requireContext.resources.getString(R.string.all_dates)
            if(selectedItemPosition <=0){
                textView.setTextColor(selectedColor)

            }
       }


//        textView.setOnClickListener {
//            dateClickListener?.let { click ->
//                click(position, date.time)
//
//                selectedItemPosition = position
//
//            }
//        }


        return view
    }

//    private val calendar = Calendar.getInstance()

//    private val formatLess = SimpleDateFormat(SHORT_DATE_FORMAT, Locale.getDefault())

     var selectedItemPosition = 0
     var selectedColor = 0

//    private var dateClickListener : ((position : Int, time : Long) -> Unit)? = null
//
//    fun setDateClickListener (listener : (position : Int, time : Long) -> Unit){
//        dateClickListener = listener
//    }

}