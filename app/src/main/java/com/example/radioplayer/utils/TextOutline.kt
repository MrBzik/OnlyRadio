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


class TextOutline(context : Context, attrs : AttributeSet?) : AppCompatTextView(context, attrs) {



//    private var _strokeColor = ContextCompat.getColor(context, R.color.outline_stroke)

    private var _strokeColor = Color.BLACK
    private var _strokeWidth = 0.8f


    private var isDrawing = true


    override fun onDraw(canvas: Canvas) {

        Log.d("CHECKTAGS", "onDraw")

            isDrawing = true
            setTextColor(_strokeColor)
            paint.strokeWidth = dpToPx(_strokeWidth)
            paint.style = Paint.Style.STROKE
            isDrawing = false
            super.onDraw(canvas)


    }

    override fun invalidate() {
        Log.d("CHECKTAGS", "invalidate")
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