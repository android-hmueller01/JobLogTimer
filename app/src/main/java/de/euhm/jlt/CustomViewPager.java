/*
 * @file CustomViewPager.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom ViewPager to catch swipe event within statistics
 * @author hmueller
 */
public class CustomViewPager extends ViewPager {

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int currentPosition = getCurrentItem();
		if (currentPosition == 0) {
			// do only check in first (START) screen
			View view = getRootView();
			int y1 = view.findViewById(R.id.layout_main_statistics).getTop();
			int y2 = view.findViewById(R.id.layout_main_statistics).getBottom();
			int scrollY = view.findViewById(R.id.main_scroll_view).getScrollY();
			float y = event.getY() + scrollY;
			if (y > y1 && y < y2) {
				// if swipe is in this region do not handle by ViewPager ...
				return false;
			}
		}
		return super.onInterceptTouchEvent(event);
	}
}