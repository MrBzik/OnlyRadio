package com.example.radioplayer.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.ListPopupWindow
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Utils {

     fun getNavigationBarHeight(context : Context): Int {
        val resources = context.resources

        val resName = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            "navigation_bar_height"
        } else {
            "navigation_bar_height_landscape"
        }

        val id: Int = resources.getIdentifier(resName, "dimen", "android")

        return if (id > 0) {
            resources.getDimensionPixelSize(id)
        } else {
            0
        }
    }


    fun getStatusBarHeight(context: Context): Int {

        val resources = context.resources

        val resName = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            "status_bar_height"
        } else {
            "status_bar_height_landscape"
        }

        var result = 0
        val resourceId: Int = context.resources.getIdentifier(resName, "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


    fun fromDateToString (calendar: Calendar) : String {

        val result = StringBuilder()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)

        val year = calendar.get(Calendar.YEAR)

        result.append("$day of ")
        result.append(
            when(month){
                0 -> "January"
                1 -> "February"
                2 -> "March"
                3 -> "April"
                4 -> "May"
                5 -> "June"
                6 -> "July"
                7 -> "August"
                8 -> "September"
                9 -> "October"
                10 -> "November"
                else -> "December"
            }
        )
        result.append(", $year")

        return result.toString()
    }


    fun fromDateToStringShort (calendar: Calendar) : String {

        val result = StringBuilder()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)


        result.append("$day of ")
        result.append(
            when(month){
                0 -> "Jan"
                1 -> "Feb"
                2 -> "Mar"
                3 -> "Apr"
                4 -> "May"
                5 -> "Jun"
                6 -> "Jul"
                7 -> "Aug"
                8 -> "Sep"
                9 -> "Oct"
                10 -> "Nov"
                else -> "Dec"
            }
        )


        return result.toString()
    }


     fun convertLongToDate(time : Long) : String {
        val date = Date(time)
        val format = DateFormat.getDateTimeInstance()
        return format.format(date)
    }

    fun convertLongToDate(time : Long, isOnlyTime : Boolean) : String {
        val date = Date(time)
        val format = DateFormat.getTimeInstance()
        return format.format(date)
    }


    fun timerFormat(mills : Long) : String {

        var milliseconds = mills

        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)

    }


    fun timerFormatCut(mills : Long) : String {

        var milliseconds = mills
        var mode = 1

        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        if(hours != 0L) mode = 3
        milliseconds -= TimeUnit.HOURS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        if(minutes != 0L && mode == 1) mode = 2
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)

        return when(mode){
            3 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            2 -> String.format("%02d:%02d", minutes, seconds)
            else ->   String.format("%02d", seconds)
        }
    }



    fun AppCompatSpinner.dismiss() {
        val popup = AppCompatSpinner::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val listPopupWindow = popup.get(this) as ListPopupWindow
        listPopupWindow.dismiss()
    }

}