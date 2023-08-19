package com.onlyradio.radioplayer.utils

import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_DEF
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_HIGH
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_LOW
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_MEDIUM
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_ULTRA

object RecPref {

     fun qualityFloatToInt(value : Float) : Int {

        return when (value) {
            REC_QUALITY_LOW -> 0
            REC_QUALITY_MEDIUM -> 1
            REC_QUALITY_DEF -> 2
            REC_QUALITY_HIGH -> 3
            REC_QUALITY_ULTRA -> 4
            else -> 5
        }
    }


}