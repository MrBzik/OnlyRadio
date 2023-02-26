package com.example.radioplayer.ui.animations

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.transition.Transition
import android.transition.TransitionValues
import android.util.Log
import android.view.View
import android.view.ViewGroup

class CustomFadeOutTransition : Transition() {

    override fun captureStartValues(transitionValues: TransitionValues) {
        // no-op
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        // no-op
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {

        Log.d("CHECKTAGS", "play")
        val view = startValues?.view ?: return null
        return AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
            )
            duration = 300
        }
    }
}