package com.example.radioplayer.utils


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.example.radioplayer.R


class TextCutIn(context : Context, attrs : AttributeSet?) : AppCompatTextView(context, attrs) {

//    private var _strokeColor = Color.WHITE
//    private var textColor = Color.BLACK

    private var _strokeColor = ContextCompat.getColor(context, R.color.main_background)
    private var textColor = ContextCompat.getColor(context, R.color.playlist_cover_text)
    private var _strokeWidth = 6f



    private var isDrawing = true



    override fun onDraw(canvas: Canvas) {

//        Log.d("CHECKTAGS", "onDraw")

        if (_strokeWidth > 0) {
            isDrawing = true

            setTextColor(_strokeColor)
            paint.strokeWidth = dpToPx(_strokeWidth)
            paint.style = Paint.Style.STROKE
            super.onDraw(canvas)

            setTextColor(textColor)

            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL
            isDrawing = false
            super.onDraw(canvas)
        } else {
            super.onDraw(canvas)
        }
    }

    override fun invalidate() {
//        Log.d("CHECKTAGS", "invalidate")
        if (isDrawing) return
        super.invalidate()
    }



   private fun dpToPx(dp: Float): Float {

        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )

    }

}