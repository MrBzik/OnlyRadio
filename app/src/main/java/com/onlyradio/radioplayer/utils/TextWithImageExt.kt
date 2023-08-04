package com.onlyradio.radioplayer.utils

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun TextView.addImage(atText: String, @DrawableRes imgSrc: Int, imgWidth: Int, imgHeight: Int) {
    val ssb = SpannableStringBuilder(this.text)

    val drawable = ContextCompat.getDrawable(this.context, imgSrc) ?: return
    drawable.mutate()
    drawable.setBounds(0, 0,
        imgWidth,
        imgHeight)
    val start = text.indexOf(atText)
    ssb.setSpan(VerticalImageSpan(drawable), start, start + atText.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    this.setText(ssb, TextView.BufferType.SPANNABLE)
}

