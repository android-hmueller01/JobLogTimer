/**
 * @file CustomApplication.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt

import android.app.Application

/**
 * Custom application to keep track of the activity visibility
 * @author hmueller
 */
object CustomApplication : Application() {
    var isActivityVisible: Boolean = false
        private set

    fun activityResumed() {
        isActivityVisible = true
    }

    fun activityPaused() {
        isActivityVisible = false
    }
}
