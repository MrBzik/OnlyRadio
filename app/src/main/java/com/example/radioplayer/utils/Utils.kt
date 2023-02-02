package com.example.radioplayer.utils

import java.lang.StringBuilder
import java.util.*

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






}