package com.onlyradio.radioplayer.extensions

import android.view.View

fun View.observeKeyboardState(
    whenKeyboardOpen : () -> Unit,
    whenKeyboardClosed : () -> Unit,
    requestFocus : () -> Unit
){
    var isFirstRun = true
    var fullScreenSize = 0
    var lastEmission = false

    this.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->


        if(isFirstRun){
            fullScreenSize = height
            isFirstRun = false
        } else{
            if(fullScreenSize > height && !lastEmission){


                lastEmission = true
                post {
                    whenKeyboardOpen()
                    postDelayed(
                        {
                        requestFocus()
                    }, 20)
                }

            } else if(fullScreenSize == height && lastEmission) {
                lastEmission = false

                post {
                    whenKeyboardClosed()

                }
            }
        }
    }



}