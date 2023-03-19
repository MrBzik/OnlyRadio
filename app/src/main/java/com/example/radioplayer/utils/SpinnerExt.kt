package com.example.radioplayer.utils

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatSpinner


class SpinnerExt(context : Context, attr : AttributeSet?) : AppCompatSpinner(context, attr) {

    interface OnSpinnerEventsListener {
        /**
         * Callback triggered when the spinner was opened.
         */
        fun onSpinnerOpened(spinner: Spinner?)

        /**
         * Callback triggered when the spinner was closed.
         */
        fun onSpinnerClosed(spinner: Spinner?)
    }

    private var mListener: OnSpinnerEventsListener? = null
    private var mOpenInitiated = false

    // implement the Spinner constructors that you need

    // implement the Spinner constructors that you need
    override fun performClick(): Boolean {
        // register that the Spinner was opened so we have a status
        // indicator for when the container holding this Spinner may lose focus
        mOpenInitiated = true

            mListener?.onSpinnerOpened(this)

        return super.performClick()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasBeenOpened() && hasFocus) {
            performClosedEvent()
        }
    }

    /**
     * Register the listener which will listen for events.
     */
    fun setSpinnerEventsListener(
        onSpinnerEventsListener: OnSpinnerEventsListener?
    ) {
        mListener = onSpinnerEventsListener
    }

    /**
     * Propagate the closed Spinner event to the listener from outside if needed.
     */
    private fun performClosedEvent() {
        mOpenInitiated = false

            mListener?.onSpinnerClosed(this)

    }

    /**
     * A boolean flag indicating that the Spinner triggered an open event.
     *
     * @return true for opened Spinner
     */
    private fun hasBeenOpened(): Boolean {
        return mOpenInitiated
    }


    override fun getWindowVisibleDisplayFrame(outRect: Rect?) {

        outRect?.set(outRect.left, outRect.top - 120, outRect.right, outRect.bottom)
    }



}