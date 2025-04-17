/**
 * @file TimesWork.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.dao

import android.content.Context
import androidx.core.content.edit
import de.euhm.jlt.utils.TimeUtil
import java.util.Calendar

/**
 * This class is the model and contains the current work times data.
 *
 * @author hmueller
 */
object TimesWork { // create a singleton (static) class
    private const val PREFS_NAME = "JobLogData"

    /** true, if work is started */
    var workStarted: Boolean = false

    /** current start time in milliseconds */
    var timeStart: Long = 0L

    /** current end time in milliseconds */
    var timeEnd: Long = 0L

    /** time already worked that day in milliseconds */
    var timeWorked: Long = 0L

    /** true, if work is in home office */
    var homeOffice: Boolean = false

    /** Calendar of current statistics view */
    var statisticsDate: Calendar = Calendar.getInstance()

    /** view filter month (Jan. = 1), set to 0 to disable */
    var filterMonth: Int = 0

    /** view filter year, set to 0 to disable */
    var filterYear: Int = 0

    /**
     * Get / set the start time of work (Calendar).
     *
     * @return Calendar value of timeStart.
     */
    var calStart: Calendar
        get() = TimeUtil.millisToCalendar(timeStart)
        set(cal) {
            timeStart = cal.timeInMillis
        }

    /**
     * Get / set the end time of work (Calendar).
     *
     * @return Calendar value of timeEnd.
     */
    var calEnd: Calendar
        get() = TimeUtil.millisToCalendar(timeEnd)
        set(cal) {
            timeEnd = cal.timeInMillis
        }

    /**
     * Restoring all data from persistent key-value data
     *
     * @param context The context used for the preferences values.
     */
    fun loadTimesWork(context: Context) {
        // Restore persistent key-value data
        val prefData = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        timeStart = prefData.getLong("timeStart", -1)
        timeEnd = prefData.getLong("timeEnd", -1)
        timeWorked = prefData.getLong("timeWorked", 0)
        workStarted = prefData.getBoolean("workStarted", false)
        homeOffice = prefData.getBoolean("homeOffice", false)
        val statistics = prefData.getLong("statistics", -1)
        if (statistics == -1L) {
            statisticsDate = TimeUtil.getCurrentTime()
        } else {
            statisticsDate.timeInMillis = statistics
        }
        filterMonth = prefData.getInt("filterMonth", 0)
        filterYear = prefData.getInt("filterYear", 0)
    }

    /**
     * Saving all data in persistent key-value data
     *
     * @param context The context used for the preferences values.
     */
    fun saveTimesWork(context: Context) {
        // Store persistent key-value data
        val prefData = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefData.edit {
            putLong("timeStart", timeStart)
            putLong("timeEnd", timeEnd)
            putLong("timeWorked", timeWorked)
            putBoolean("homeOffice", homeOffice)
            putBoolean("workStarted", workStarted)
            putLong("statistics", statisticsDate.timeInMillis)
            putInt("filterMonth", filterMonth)
            putInt("filterYear", filterYear)
        } // Commit the edits
    }

    /**
     * Calc normal work time based on start time
     *
     * @param context The context used for the preferences values.
     * @return Normal work end time
     */
    fun normalWorkEndTime(context: Context): Long {
        return TimeUtil.getNormalWorkEndTime(context, timeStart, timeWorked, homeOffice)
    }

    /**
     * Calc normal work time based on start time
     *
     * @param context The context used for the preferences values.
     * @return Normal work end time (Calendar class)
     */
    fun calNormalWorkEndTime(context: Context): Calendar {
        return TimeUtil.millisToCalendar(normalWorkEndTime(context))
    }

    /**
     * Calc max. work time based on start time
     *
     * @param context The context used for the preferences values.
     * @return Max. work end time
     */
    fun maxWorkEndTime(context: Context): Long {
        return TimeUtil.getMaxWorkEndTime(context, timeStart, timeWorked, homeOffice)
    }

    /**
     * Calc max. work time based on start time
     *
     * @param context The context used for the preferences values.
     * @return Max. work end time (Calendar class)
     */
    fun calMaxWorkEndTime(context: Context): Calendar {
        return TimeUtil.millisToCalendar(maxWorkEndTime(context))
    }
}
