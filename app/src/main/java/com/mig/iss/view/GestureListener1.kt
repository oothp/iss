package com.mig.iss.view

import android.view.GestureDetector
import android.view.MotionEvent
import com.mig.iss.model.enums.Direction
import kotlin.math.atan2

// https://stackoverflow.com/a/26387629
class GestureListener1 : GestureDetector.SimpleOnGestureListener() {

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            super.onScroll(e1, e2, distanceX, distanceY)
            doSomethingWithDirection(getSwipeDirection(e1.x, e1.y, e2.x, e2.y))
            return false
        }

//    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
//        super.onFling(e1, e2, velocityX, velocityY)
//        doSomethingWithDirection(e1, e2, getSwipeDirection(e1.x, e1.y, e2.x, e2.y))
//        return false
//    }

    private fun doSomethingWithDirection(swipeDirection: Direction) {
        if (swipeDirection == Direction.DOWN) {
//            onSwipeDownCallback.onSwipeDown()
        }
    }

    private fun getSwipeDirection(x1: Float, y1: Float, x2: Float, y2: Float): Direction {
        val angle = getAngle(x1, y1, x2, y2)
        return when {
            angle >= 45f && angle < 135f -> Direction.UP
            angle >= 225f && angle < 315f -> Direction.DOWN
            angle >= 0f && angle < 45f -> Direction.RIGHT
            else -> Direction.LEFT
        }
    }

    /**
     * Finds the angle between two points in the plane (x1,y1) and (x2, y2)
     * The angle is measured with 0/360 being the X-axis to the right, angles
     * increase counter clockwise.
     *
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the angle between two points
     */
    private fun getAngle(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val rad = atan2((y1 - y2).toDouble(), (x2 - x1).toDouble()) + Math.PI
        return (rad * 180 / Math.PI + 180) % 360
    }

}