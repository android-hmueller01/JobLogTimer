/**
 * @file Constants.kt
 * 
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.utils

/**
 * Helper class for global constants
 * @author hmueller
 */
object Constants {
    // Notification IDs
    const val NOTIFICATION_END_WORK: Int = 1
    const val NOTIFICATION_CHANNEL: String = "WORK DONE"

    // Action Codes
    //public static final String ACTION_UPDATE_WIDGET = "de.euhm.jlt.ACTION_UPDATE_WIDGET";
    // use AppWidgetManager.ACTION_APPWIDGET_UPDATE instead
    // Receiver Codes
    const val RECEIVER_RECREATE: String = "de.euhm.jlt.RECREATE"
    const val RECEIVER_UPDATE_VIEW: String = "de.euhm.jlt.UPDATE_VIEW"
    const val RECEIVER_NORMAL_WORK_ALARM: String = "de.euhm.jlt.NORMAL_WORK_ALARM"
    const val RECEIVER_MAX_WORK_ALARM: String = "de.euhm.jlt.MAX_WORK_ALARM"
    const val RECEIVER_START_STOP: String = "de.euhm.jlt.START_STOP"

    // Permission Codes
    //const val PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: Int = 1

    // Button IDs
    const val BUTTON_OK: Int = 0x100001
    const val BUTTON_DELETE: Int = 0x100002
    const val BUTTON_CANCEL: Int = 0x100003
    const val BUTTON_CLEAR: Int = 0x100004

    // Other constants
    const val WIDGET_UPDATE_INTERVAL: Long = 60 * 1000 // 1 minute

    // break times by German law
    const val GL_WORK_TIME1: Long = 6 * 60 * 60 * 1000 // 6 h
    const val GL_WORK_TIME2: Long = 9 * 60 * 60 * 1000 // 9 h
    const val GL_BREAK_TIME1: Long = 30 * 60 * 1000 // 30 min.
    const val GL_BREAK_TIME2: Long = 45 * 60 * 1000 // 45 min.
}
