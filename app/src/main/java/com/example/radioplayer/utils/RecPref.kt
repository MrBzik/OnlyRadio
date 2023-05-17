package com.example.radioplayer.utils

import com.example.radioplayer.utils.Constants.REC_QUALITY_DEF
import com.example.radioplayer.utils.Constants.REC_QUALITY_HIGH
import com.example.radioplayer.utils.Constants.REC_QUALITY_LOW
import com.example.radioplayer.utils.Constants.REC_QUALITY_MEDIUM
import com.example.radioplayer.utils.Constants.REC_QUALITY_ULTRA

object RecPref {

     fun qualityFloatToInt(value : Float) : Int {

        return if(value == REC_QUALITY_LOW) 1
        else if(value == REC_QUALITY_MEDIUM) 2
        else if(value == REC_QUALITY_DEF) 3
        else if(value < REC_QUALITY_HIGH) 4
        else if(value < REC_QUALITY_ULTRA) 5
        else 6
    }

    fun setTvRecQualityValue(value : Int) : String {

        return when(value){
            1 -> "Low"
            2  -> "Medium"
            3  -> "Normal"
            4  -> "High"
            5 -> "Ultra"
            else -> "Max"
        }
    }


}