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



    private var interactiveColor = ContextCompat.getColor(context, R.color.outlined_text_interactive_color)

    private var _strokeColor = Color.WHITE
    private var _strokeWidth = 0.4f

    var isSingleColor = false

    var colors = intArrayOf(
        Color.WHITE, interactiveColor
    )

    fun setStrokeColor(color: Int) {
        _strokeColor = color
    }

    fun setStrokeWidth(width: Float) {
        _strokeWidth = width
    }


    fun setColors(colorDefault : Int){

        colors = intArrayOf(colorDefault, Color.RED)

    }


    private var isDrawing = true


    private val states = arrayOf(
        intArrayOf(-android.R.attr.state_pressed),
        intArrayOf(android.R.attr.state_pressed)
    )

   private val stateList : ColorStateList by lazy {
        ColorStateList(states, colors)
    }


    override fun onDraw(canvas: Canvas) {

//        Log.d("CHECKTAGS", "onDraw")

        if (_strokeWidth > 0) {
            isDrawing = true
            val textColor = textColors.defaultColor
            setTextColor(_strokeColor)
            paint.strokeWidth = dpToPx(_strokeWidth)
            paint.style = Paint.Style.STROKE
            super.onDraw(canvas)

            if(isSingleColor)
                setTextColor(textColor)
            else
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