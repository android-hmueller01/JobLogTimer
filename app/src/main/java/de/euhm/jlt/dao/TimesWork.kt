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
import android.content.SharedPreferences
import androidx.core.content.edit
import de.euhm.jlt.utils.TimeUtil
import java.util.Calendar


private const val PREFS_NAME = "JobLogData"
private const val WORK_STARTED_KEY = "workStarted"
private const val TIME_START_KEY = "timeStart"
private const val TIME_END_KEY = "timeEnd"
private const val TIME_WORKED_KEY = "timeWorked"
private const val HOME_OFFICE_KEY = "homeOffice"
private const val STATISTICS_KEY = "statistics"
private const val FILTER_MONTH_KEY = "filterMonth"
private const val FILTER_YEAR_KEY = "filterYear"

/**
 * This class contains the current work times data.
 *
 * @author hmueller
 */
class TimesWork(val context: Context) {
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** true, if work is started */
    var workStarted: Boolean
        get() = (sharedPreferences.getBoolean(WORK_STARTED_KEY, false))
        set(value) {
            sharedPreferences.edit { putBoolean(WORK_STARTED_KEY, value) }
        }

    /** current start time in milliseconds */
    var timeStart: Long
        get() = (sharedPreferences.getLong(TIME_START_KEY, -1L))
        set(value) {
            sharedPreferences.edit { putLong(TIME_START_KEY, value) }
        }

    /** current end time in milliseconds */
    var timeEnd: Long
        get() = (sharedPreferences.getLong(TIME_END_KEY, -1L))
        set(value) {
            sharedPreferences.edit { putLong(TIME_END_KEY, value) }
        }

    // TODO: do not use this in statistics as db might change and will not reflect that
    /** time already worked that day in milliseconds */
    var timeWorked: Long
        get() = (sharedPreferences.getLong(TIME_WORKED_KEY, -1L))
        set(value) {
            sharedPreferences.edit { putLong(TIME_WORKED_KEY, value) }
        }

    /** true, if work is in home office */
    var homeOffice: Boolean
        get() {
            return (sharedPreferences.getBoolean(HOME_OFFICE_KEY, false))
        }
        set(value) {
            sharedPreferences.edit { putBoolean(HOME_OFFICE_KEY, value) }
        }

    /** Calendar of current statistics view */
    var statisticsDate: Calendar
        get() {
            val statistics = sharedPreferences.getLong(STATISTICS_KEY, -1L)
            return if (statistics == -1L) {
                TimeUtil.getCurrentTime()
            } else {
                TimeUtil.millisToCalendar(statistics)
            }
        }
        set(value) {
            sharedPreferences.edit { putLong(STATISTICS_KEY, value.timeInMillis) }
        }

    /** view filter month (Jan. = 1), set to 0 to disable */
    var filterMonth: Int
        get() {
            return (sharedPreferences.getInt(FILTER_MONTH_KEY, 0))
        }
        set(value) {
            sharedPreferences.edit { putInt(FILTER_MONTH_KEY, value) }
        }

    /** view filter year, set to 0 to disable */
    var filterYear: Int
        get() {
            return (sharedPreferences.getInt(FILTER_YEAR_KEY, 0))
        }
        set(value) {
            sharedPreferences.edit { putInt(FILTER_YEAR_KEY, value) }
        }

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
