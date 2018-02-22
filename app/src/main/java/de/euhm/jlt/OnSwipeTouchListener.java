/*
 * @file OnSwipeTouchListener.java
 * 
 * based on Android: How to handle right to left swipe gestures
 * see http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
 * and Android GestureDetector class
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * Handle swipes and taps on a view.<br>
 * Usage:<br><code>
 * View view = findViewById(R.id.view);<br>
 * mOnSwipeTouchListener = new OnSwipeTouchListener(ActivityMain.this, view) {<br>
 *   &#64;Override
 *   public boolean onSwipeRight() {
 *     Toast.makeText(ActivityMain.this, "right", Toast.LENGTH_SHORT).show();
 *     return true;
 *   }<br>
 *   &#64;Override
 *   public boolean onSwipeLeft() {
 *     Toast.makeText(ActivityMain.this, "left", Toast.LENGTH_SHORT).show();
 *     return true;
 *   }<br>
 *   &#64;Override
 *   public boolean onDoubleTap() {
 *     Toast.makeText(ActivityMain.this, "double tap", Toast.LENGTH_SHORT).show();
 *     return true;
 *   }<br>
 * };<br>
 * view.setOnTouchListener(mOnSwipeTouchListener);
 * </code>
 * @author hmueller
 */
public class OnSwipeTouchListener implements OnTouchListener {

	/**
	 * Distance in dp a touch has to wander before we think the user is swiping.
	 */
	private static final int MIN_SWIPE_DISTANCE = 100; // in dp
    // constants for Message.what used by GestureHandler below
    private static final int TAP = 1;
    private final Handler mHandler;

	/**
	 * The duration in milliseconds between the first tap's up event and the 
	 * second tap's down event for an interaction to be considered a double-tap.
	 */
    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    private static final int DOUBLE_TAP_MIN_TIME = 10;
	/**
	 * The duration in milliseconds we will wait to see if a touch event is
	 * a tap or a scroll. If the user does not move more than {@link #mTouchSlop} 
	 * within this interval, it is considered to be a tap.
	 */
    private static final int TAB_TIMEOUT= ViewConfiguration.getTapTimeout();

    /**
     * Duration of animation translation in milliseconds.
     */
	private static final long ANIMATION_DURATION = 300;

	private final View mView;
	/**
	 * Distance in pixels a touch can wander before we think the user is scrolling.
	 */
	private final int mTouchSlop;
	/**
	 * Distance in pixels a touch has to wander before we think the user is swiping.
	 */
	private final int mSwipeSlop;
	/**
     * True when the user is still touching for the second tap (down, move, and
     * up events).
     */
    private boolean mIsDoubleTapping;
	/**
	 * Square distance in pixels between the first touch and second touch 
	 * to still be considered a double tap.
	 */
    private int mDoubleTapSlopSquare;
	/**
     * True as long the user is touching the screen within the view.
     */
    private boolean mIsDown;
	/**
	 * The logical density of the display. This is a scaling factor for the 
	 * Density Independent Pixel unit.
	 */
	private final float mDpScale;
	/**
	 * The absolute width of the display in pixels.
	 */
	private final int mWidthPixels;
	
	private MotionEvent mCurrentDownEvent;
	private MotionEvent mPreviousUpEvent;

	public OnSwipeTouchListener(Context context, View view) {
		mView = view;

        mDpScale = context.getResources().getDisplayMetrics().density;
        mWidthPixels = context.getResources().getDisplayMetrics().widthPixels;

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        int doubleTapSlop = configuration.getScaledDoubleTapSlop();
        mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop;
        mSwipeSlop = (int) (MIN_SWIPE_DISTANCE * mDpScale);

        mHandler = new GestureHandler(this);
	}

	public View getView() {
		return mView;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// return false to let ScrollView handle the event ...
		boolean result = false;

		// get x1 (position of last current down event) and x2 (position of this call)
		float x2 = event.getX();
		float x1 = x2;
		if (mCurrentDownEvent != null) {
			x1 = mCurrentDownEvent.getX();
		}
		float deltaX = x2 - x1;

		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// get location of view on screen
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			int viewTop = location[1];
			int viewBottom = viewTop + view.getHeight();
			int y = (int) event.getY();
			if (y > viewTop && y < viewBottom) {
				// handle only, if ACTION_DOWN is within region of view
				if ((mCurrentDownEvent != null) && (mPreviousUpEvent != null) &&
						isConsideredDoubleTap(mCurrentDownEvent, mPreviousUpEvent, event)) {
					// This is a second tap
					mIsDoubleTapping = true;
				} else {
					mIsDoubleTapping = false;
				}
				if (mCurrentDownEvent != null) {
					mCurrentDownEvent.recycle();
				}
				mCurrentDownEvent = MotionEvent.obtain(event);
				mIsDown = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (!mIsDown) {
				// ignore ACTION_UP if there was no ACTION_DOWN detected within view
				break;
			}
			view.performClick();

            if (mPreviousUpEvent != null) {
                mPreviousUpEvent.recycle();
            }
            // Hold the event we obtained above - listeners may have changed the original.
            mPreviousUpEvent = MotionEvent.obtain(event);

            // get time between last ACTION_DOWN and current ACTION_UP event
			long dt = TAB_TIMEOUT;
			if (mCurrentDownEvent != null) {
				dt = event.getEventTime() - mCurrentDownEvent.getEventTime();
			}

			// get and reset translation from ACTION_MOVE event
			float fromXDelta = view.getTranslationX();
			view.setTranslationX(0);

            if (mIsDoubleTapping) {
                // Finally, call the double-tap event
            	result = onDoubleTap();
            	// reset mIsDoubleTapping in mHandler ...
            	//mIsDoubleTapping = false;
				result = true;
            } else if (Math.abs(deltaX) > mSwipeSlop) {
            	// consider as swipe
				float toXDelta;
				if (x2 > x1) {
					// left to right swipe action
					result = onSwipeRight();
					toXDelta = mWidthPixels;
				} else {
					// right to left swipe action
					result = onSwipeLeft();
					toXDelta = -mWidthPixels;
				}
				TranslateAnimation anim;
				if (result) {
					anim = new TranslateAnimation(fromXDelta, toXDelta, 0, 0);
					anim.setInterpolator(new AccelerateInterpolator());
				} else {
					anim = new TranslateAnimation(fromXDelta, 0, 0, 0);
					anim.setInterpolator(new AccelerateDecelerateInterpolator());
				}
				anim.setDuration(ANIMATION_DURATION);
				//anim.setFillAfter(false);
				view.startAnimation(anim);
				result = true;
			} else if (Math.abs(deltaX) < mTouchSlop && dt < TAB_TIMEOUT) {
				// consider as a single screen tap, wait if there is a second (double tab)
                mHandler.sendEmptyMessageDelayed(TAP, DOUBLE_TAP_TIMEOUT);
				result = false;
			} else {
				// cancel operation and move view back
				TranslateAnimation anim = new TranslateAnimation(fromXDelta, 0, 0, 0);
				anim.setInterpolator(new AccelerateDecelerateInterpolator());
				anim.setDuration(ANIMATION_DURATION);
				//anim.setFillAfter(true);
				view.startAnimation(anim);
			}
            mIsDown = false;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mIsDown) {
				view.setTranslationX(deltaX);
			}
			break;
		}

		return result;
	}

    /**
     * Check for double tap.
     * @param firstDown First down motion event.
     * @param firstUp First up motion event.
     * @param secondDown Current motion event.
     * @return <code>True</code>, if a double tap is detected.
     */
	private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp,
            MotionEvent secondDown) {
        final long deltaTime = secondDown.getEventTime() - firstUp.getEventTime();
        if (deltaTime > DOUBLE_TAP_TIMEOUT || deltaTime < DOUBLE_TAP_MIN_TIME) {
            return false;
        }

        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        return (deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare);
    }

    private static class GestureHandler extends Handler {
		/*
		 * Lint warning: This Handler class should be static or leaks might occur.
		 * 
		 * If GestureHandler class is not static, it will have a reference to
		 * your Service object.
		 * 
		 * Handler objects for the same thread all share a common Looper object,
		 * which they post messages to and read from.
		 * 
		 * As messages contain target Handler, as long as there are messages
		 * with target handler in the message queue, the handler cannot be
		 * garbage collected. If handler is not static, your Service or Activity
		 * cannot be garbage collected, even after being destroyed.
		 * 
		 * This may lead to memory leaks, for some time at least - as long as
		 * the messages stays in the queue. This is not much of an issue unless
		 * you post long delayed messages.
		 * 
		 * You can make the handler static and have a WeakReference to your
		 * service.
		 */
    	private final WeakReference<OnSwipeTouchListener> myClassWeakReference;

    	GestureHandler(OnSwipeTouchListener myClassInstance) {
    	      myClassWeakReference = new WeakReference<>(myClassInstance);
    	}

        @Override
        public void handleMessage(Message msg) {
			OnSwipeTouchListener classRef = myClassWeakReference.get();
			if (classRef != null) {
				switch (msg.what) {
				case TAP:
					// If the user's finger is still down, do not count it as a tap
					if (classRef.mIsDoubleTapping) {
						classRef.mIsDoubleTapping = false;
					} else {
						classRef.onSingleTap();
					}
					break;

				default:
					throw new RuntimeException("Unknown message " + msg); // never
				}
			}
		}
	} // end class GestureHandler

	/**
	 * Override this to get a right swipe event.
	 * @return <code>True</code>, if event was handled and no other action should be taken.
	 */
    public boolean onSwipeRight() {
    	return false;
    }

	/**
	 * Override this to get a left swipe event.
	 * @return <code>True</code>, if event was handled and no other action should be taken.
	 */
	public boolean onSwipeLeft() {
		return false;
	}

	/**
	 * Override this to get a single tap event.
	 */
	public void onSingleTap() {}

	/**
	 * Override this to get a double tap event.
	 * @return <code>True</code>, if event was handled and no other action should be taken.
	 */
	public boolean onDoubleTap() {
		return false;
	}
}