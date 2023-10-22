package com.onlyradio.radioplayer.extensions

import android.content.Context
import android.widget.Toast

fun Context.makeToast(messageRes : Int, length : Int = Toast.LENGTH_SHORT){

    val message = resources.getString(messageRes)

    Toast.makeText(this, message, length).show()

}