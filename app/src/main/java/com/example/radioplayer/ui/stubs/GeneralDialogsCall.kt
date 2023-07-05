package com.example.radioplayer.ui.stubs

interface GeneralDialogsCall {

     fun recOptionsDialog(newValue : (Int) -> Unit)
     fun recInitialValue() : Int
     fun historyDialog()
     fun bufferDialog()

}