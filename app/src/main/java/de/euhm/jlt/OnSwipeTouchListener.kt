/**
 * @file OnSwipeTouchListener.kt
 *
 * based on Android: How to handle right to left swipe gestures
 * see http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
 * and Android GestureDetector class
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.TranslateAnimation
import java.lang.ref.WeakReference
import kotlin.math.abs

/**
 * Handle swipes and taps on a view.<br></br>
 * Usage:<br></br>`
 * View view = findViewById(R.id.view);<br></br>
 * mOnSwipeTouchListener = new OnSwipeTouchListener(ActivityMain.this, view) {<br></br>
 * &#64;Override
 * public boolean onSwipeRight() {
 * Toast.makeText(ActivityMain.this, "right", Toast.LENGTH_SHORT).show();
 * return true;
 * }<br></br>
 * &#64;Override
 * public boolean onSwipeLeft() {
 * Toast.makeText(ActivityMain.this, "left", Toast.LENGTH_SHORT).show();
 * return true;
 * }<br></br>
 * &#64;Override
 * public boolean onDoubleTap() {
 * Toast.makeText(ActivityMain.this, "double tap", Toast.LENGTH_SHORT).show();
 * return true;
 * }<br></br>
 * };<br></br>
 * view.setOnTouchListener(mOnSwipeTouchListener);
` *
 *
 * @author hmueller
 */
open class OnSwipeTouchListener(context: Context, val view: View) : OnTouchListener {
    private val mHandler: Handler

    /**
     * Distance in pixels a touch can wander before we think the user is scrolling.
     */
    private val mTouchSlop: Int

    /**
     * Distance in pixels a touch has to wander before we think the user is swiping.
     */
    private val mSwipeSlop: Int

    /**
     * True when the user is still touching for the second tap (down, move, and
     * up events).
     */
    private var mIsDoubleTapping = false

    /**
     * Square distance in pixels between the first touch and second touch
     * to still be considered a double tap.
     */
    private val mDoubleTapSlopSquare: Int

    /**
     * True as long the user is touching the screen within the view.
     */
    private var mIsDown = false

    /**
     * The logical density of the display. This is a scaling factor for the
     * Density Independent Pixel unit.
     */
    private val mDpScale = context.resources.displayMetrics.density

    /**
     * The absolute width of the display in pixels.
     */
    private val mWidthPixels = context.resources.displayMetrics.widthPixels

    private var mCurrentDownEvent: MotionEvent? = null
    private var mPreviousUpEvent: MotionEvent? = null

    init {
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        val doubleTapSlop = configuration.scaledDoubleTapSlop
        mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop
        mSwipeSlop = (MIN_SWIPE_DISTANCE * mDpScale).toInt()

        mHandler = GestureHandler(this, Looper.getMainLooper())
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        // return false to let ScrollView handle the event ...
        var result = false

        // get x1 (position of last current down event) and x2 (position of this call)
        val x2 = event.x
        var x1 = x2
        if (mCurrentDownEvent != null) {
            x1 = mCurrentDownEvent!!.x
        }
        val deltaX = x2 - x1

        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                // get location of view on screen
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                val viewTop = location[1]
                val viewBottom = viewTop + view.height
                val y = event.y.toInt()
                if (y in viewTop..viewBottom) {
                    // handle only, if ACTION_DOWN is within region of view
                    // This is a second tap
                    mIsDoubleTapping = (mCurrentDownEvent != null) && (mPreviousUpEvent != null) &&
                            isConsideredDoubleTap(mCurrentDownEvent!!, mPreviousUpEvent!!, event)
                    if (mCurrentDownEvent != null) {
                        mCurrentDownEvent!!.recycle()
                    }
                    mCurrentDownEvent = MotionEvent.obtain(event)
                    mIsDown = true
                }
            }

            MotionEvent.ACTION_UP -> {
                // ignore ACTION_UP if there was no ACTION_DOWN detected within view
                if (mIsDown) {
                    view.performClick()

                    if (mPreviousUpEvent != null) {
                        mPreviousUpEvent!!.recycle()
                    }
                    // Hold the event we obtained above - listeners may have changed the original.
                    mPreviousUpEvent = MotionEvent.obtain(event)

                    // get time between last ACTION_DOWN and current ACTION_UP event
                    var dt = TAB_TIMEOUT.toLong()
                    if (mCurrentDownEvent != null) {
                        dt = event.eventTime - mCurrentDownEvent!!.eventTime
                    }

                    // get and reset translation from ACTION_MOVE event
                    val fromXDelta = view.translationX
                    view.translationX = 0f

                    if (mIsDoubleTapping) {
                        // Finally, call the double-tap event
                        result = onDoubleTap()
                        // reset mIsDoubleTapping in mHandler ...
                        //mIsDoubleTapping = false;
                        //result = true;
                    } else if (abs(deltaX.toDouble()) > mSwipeSlop) {
                        // consider as swipe
                        val toXDelta: Float
                        if (x2 > x1) {
                            // left to right swipe action
                            result = onSwipeRight()
                            toXDelta = mWidthPixels.toFloat()
                        } else {
                            // right to left swipe action
                            result = onSwipeLeft()
                            toXDelta = -mWidthPixels.toFloat()
                        }
                        val anim: TranslateAnimation
                        if (result) {
                            anim = TranslateAnimation(fromXDelta, toXDelta, 0f, 0f)
                            anim.interpolator = AccelerateInterpolator()
                        } else {
                            anim = TranslateAnimation(fromXDelta, 0f, 0f, 0f)
                            anim.interpolator = AccelerateDecelerateInterpolator()
                        }
                        anim.duration = ANIMATION_DURATION
                        //anim.setFillAfter(false);
                        view.startAnimation(anim)
                        result = true
                    } else if (abs(deltaX.toDouble()) < mTouchSlop && dt < TAB_TIMEOUT) {
                        // consider as a single screen tap, wait if there is a second (double tab)
                        mHandler.sendEmptyMessageDelayed(TAP, DOUBLE_TAP_TIMEOUT.toLong())
                    } else {
                        // cancel operation and move view back
                        val anim = TranslateAnimation(fromXDelta, 0f, 0f, 0f)
                        anim.interpolator = AccelerateDecelerateInterpolator()
                        anim.duration = ANIMATION_DURATION
                        //anim.setFillAfter(true);
                        view.startAnimation(anim)
                    }
                    mIsDown = false
                }
            }

            MotionEvent.ACTION_MOVE -> if (mIsDown) {
                view.translationX = deltaX
            }
        }

        return result
    }

    /**
     * Check for double tap.
     *
     * @param firstDown  First down motion event.
     * @param firstUp    First up motion event.
     * @param secondDown Current motion event.
     * @return `True`, if a double tap is detected.
     */
    private fun isConsideredDoubleTap(firstDown: MotionEvent, firstUp: MotionEvent,
                                      secondDown: MotionEvent): Boolean {
        val deltaTime = secondDown.eventTime - firstUp.eventTime
        if (deltaTime > DOUBLE_TAP_TIMEOUT || deltaTime < DOUBLE_TAP_MIN_TIME) {
            return false
        }

        val deltaX = firstDown.x.toInt() - secondDown.x.toInt()
        val deltaY = firstDown.y.toInt() - secondDown.y.toInt()
        return (deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare)
    }

    private class GestureHandler(myClassInstance: OnSwipeTouchListener, looper: Looper) : Handler(looper) {
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
        private val myClassWeakReference =
            WeakReference(myClassInstance)

        override fun handleMessage(msg: Message) {
            val classRef = myClassWeakReference.get()
            if (classRef != null) {
                if (msg.what == TAP) {
                    // If the user's finger is still down, do not count it as a tap
                    if (classRef.mIsDoubleTapping) {
                        classRef.mIsDoubleTapping = false
                    } else {
                        classRef.onSingleTap()
                    }
                } else {
                    throw RuntimeException("Unknown message $msg") // never
                }
            }
        }
    } // end class GestureHandler


    /**
     * Override this to get a right swipe event.
     *
     * @return `True`, if event was handled and no other action should be taken.
     */
    open fun onSwipeRight(): Boolean {
        return false
    }

    /**
     * Override this to get a left swipe event.
     *
     * @return `True`, if event was handled and no other action should be taken.
     */
    open fun onSwipeLeft(): Boolean {
        return false
    }

    /**
     * Override this to get a single tap event.
     */
    fun onSingleTap() {
    }

    /**
     * Override this to get a double tap event.
     *
     * @return `True`, if event was handled and no other action should be taken.
     */
    open fun onDoubleTap(): Boolean {
        return false
    }

    companion object {
        /**
         * Distance in dp a touch has to wander before we think the user is swiping.
         */
        private const val MIN_SWIPE_DISTANCE = 100 // in dp

        // constants for Message.what used by GestureHandler below
        private const val TAP = 1

        /**
         * The duration in milliseconds between the first tap's up event and the
         * second tap's down event for an interaction to be considered a double-tap.
         */
        private val DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout()
        private const val DOUBLE_TAP_MIN_TIME = 10

        /**
         * The duration in milliseconds we will wait to see if a touch event is
         * a tap or a scroll. If the user does not move more than [.mTouchSlop]
         * within this interval, it is considered to be a tap.
         */
        private val TAB_TIMEOUT = ViewConfiguration.getTapTimeout()

        /**
         * Duration of animation translation in milliseconds.
         */
        private const val ANIMATION_DURATION: Long = 300
    }
}