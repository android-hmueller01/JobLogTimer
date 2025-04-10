/**
 * @file CustomViewPager.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager

/**
 * Custom ViewPager to catch swipe event within statistics
 *
 * @author hmueller
 */
class CustomViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val currentPosition = currentItem
        if (currentPosition == 0) {
            // do only check in first (START) screen
            val view = rootView
            val viewLayoutMainStatistics = view.findViewById<View>(R.id.layout_main_statistics)
            val y1 = viewLayoutMainStatistics.top
            val y2 = viewLayoutMainStatistics.bottom
            val scrollY = view.findViewById<View>(R.id.main_scroll_view).scrollY
            val y = event.y + scrollY
            if (y > y1 && y < y2) {
                // if swipe is in this region do not handle by ViewPager ...
                return false
            }
        }
        return super.onInterceptTouchEvent(event)
    }
}