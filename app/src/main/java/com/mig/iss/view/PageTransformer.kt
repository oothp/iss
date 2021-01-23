package com.mig.iss.view

import android.content.Context
import android.view.View
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import com.mig.iss.R

class PageTransformer(context: Context, private val pager: ViewPager2) : ViewPager2.PageTransformer {

    private val pageMarginPx = context.resources.getDimension(R.dimen.page_margin)
    private val pageOffsetPx = context.resources.getDimension(R.dimen.page_offset)

    private var offsetPx : Float = 0f

    override fun transformPage(page: View, position: Float) {
        val offset = position * -(2 * offsetPx + pageMarginPx)
        page.translationX = when (ViewCompat.getLayoutDirection(pager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            true -> -offset
            false -> offset
        }
    }

    fun hidePagePreview(hidePreview: Boolean = true) {
        offsetPx = when (hidePreview) {
            false -> pageOffsetPx
            true -> 0f
        }
        pager.requestTransform()
    }

    fun updatePagePreviewOffset(offset: Int) {
        offsetPx = offset.toFloat()
        pager.requestTransform()
    }

    fun updatePagePreviewOffset(offset: Float) {
        offsetPx = offset
        pager.requestTransform()
    }
}