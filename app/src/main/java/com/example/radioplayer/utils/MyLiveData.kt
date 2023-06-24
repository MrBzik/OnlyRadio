package com.example.radioplayer.utils

import androidx.lifecycle.LiveData
import com.example.radioplayer.exoPlayer.RadioService

class MyLiveData<T> : LiveData<T>() {

    override fun postValue(value: T) {

        if(RadioService.isToUpdateLiveData){
            super.postValue(value)
        } else {
            RadioService.isToUpdateLiveData = true
        }
    }
}