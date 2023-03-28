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

const val DEFAULT_STROKE_WIDTH = 0

class TextViewOutlined(context : Context, attrs : AttributeSet?) : AppCompatTextView(context, attrs) {



    private var _strokeColor = Color.BLACK
    private var _strokeWidth = 1.5f

    val color = ContextCompat.getColor(context, R.color.interactive_text_color)


    fun setStrokeColor(color: Int) {
        _strokeColor = color
    }

    fun setStrokeWidth(width: Float) {
        _strokeWidth = width
    }




    private var isDrawing = true



    val colors = intArrayOf(
        Color.RED,
        Color.WHITE
    )

    val states = arrayOf(
        intArrayOf(android.R.attr.state_pressed),
        intArrayOf(-android.R.attr.state_pressed)
    )

    val stateList = ColorStateList(states, colors)


    override fun onDraw(canvas: Canvas) {

        Log.d("CHECKTAGS", "draw")

        if (_strokeWidth > 0) {
            isDrawing = true

            setTextColor(_strokeColor)
            paint.strokeWidth = dpToPx(_strokeWidth)
            paint.style = Paint.Style.STROKE
            super.onDraw(canvas)

            setTextColor(stateList)
            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL
            isDrawing = false
            super.onDraw(canvas)
        } else {
            super.onDraw(canvas)
        }
    }

    override fun invalidate() {
        Log.d("CHECKTAGS", "invalidate")
        if (isDrawing) return
        super.invalidate()
    }

    override fun setCompoundDrawablesWithIntrinsicBounds(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
    }

    fun dpToPx(dp: Float): Float {

        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )

    }

}