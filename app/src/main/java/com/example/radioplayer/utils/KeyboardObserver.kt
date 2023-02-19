package com.example.radioplayer.utils

import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.*
import java.util.*
import java.util.logging.Handler


object KeyboardObserver {


     fun observeKeyboardState(
        view: View,
        lifecycle : LifecycleCoroutineScope,
        whenKeyboardOpen : () -> Unit,
        whenKeyboardClosed : () -> Unit,
        requestFocus : () -> Unit

    ){

        var isFirstRun = true
        var fullScreenSize = 0
        var lastEmition = false



        view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->

            if(isFirstRun){
                fullScreenSize = view.height
                isFirstRun = false
            } else{
                if(fullScreenSize > view.height && !lastEmition){
                    lastEmition = true
                    lifecycle.launch {
                        delay(10)
                        withContext(Dispatchers.Main){
                            whenKeyboardOpen()
                        }
                        delay(20)
                            withContext(Dispatchers.Main){
                                requestFocus()
                            }

                        this.cancel()
                    }


                } else if(fullScreenSize == view.height && lastEmition) {
                    lastEmition = false
                    lifecycle.launch{
                        delay(50)
                        withContext(Dispatchers.Main){
                            whenKeyboardClosed()
                        }
                        this.cancel()
                    }
                }
            }
        }
    }



}




