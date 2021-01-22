package com.mig.iss.view

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class ResizeAnimation(v: View) : Animation() {

    private var startWidth = 0
    private var deltaWidth = 0 // distance between start and end height
    private val view: View = v

    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        view.layoutParams.width = (startWidth + deltaWidth * interpolatedTime).toInt()
        view.requestLayout()
    }
    /**
     * set the starting and ending height for the resize animation
     * starting height is usually the views current height, the end height is the height
     * we want to reach after the animation is completed
     * @param start height in pixels
     * @param end height in pixels
     */
    fun setParams(start: Int, end: Int) {
        startWidth = start
        deltaWidth = end - startWidth
    }

    override fun setDuration(durationMillis: Long) {
        super.setDuration(durationMillis)
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}