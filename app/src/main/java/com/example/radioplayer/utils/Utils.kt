package com.example.radioplayer.utils

import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.TimeUnit

object Utils {


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



}