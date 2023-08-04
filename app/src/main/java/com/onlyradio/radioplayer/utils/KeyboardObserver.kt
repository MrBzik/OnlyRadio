package com.onlyradio.radioplayer.utils


import android.view.View


object KeyboardObserver {


     fun observeKeyboardState(
        view: View,
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
                    view.post {
                        whenKeyboardOpen()
                            view.postDelayed({
                                requestFocus()
                            }, 20)
                    }

                } else if(fullScreenSize == view.height && lastEmition) {
                    lastEmition = false

                    view.post {
                        whenKeyboardClosed()

                    }
                }
            }
        }
    }



}




