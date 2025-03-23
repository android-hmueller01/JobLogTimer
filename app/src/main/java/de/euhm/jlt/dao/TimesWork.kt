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
class TimesWork(private val mContext: Context) {
    init {
        if (timeStart == 0L) {
            // Restore work data from persistent store,
            // if static values are destroyed
            loadTimesWork(mContext)
        }
    }

    /**
     * Calc normal work time based on start time
     *
     * @return Normal work end time
     */
    val normalWorkEndTime: Long
        get() = TimeUtil.getNormalWorkEndTime(mContext, timeStart, timeWorked, homeOffice)

    /**
     * Calc normal work time based on start time
     *
     * @return Normal work end time (Calendar class)
     */
    val calNormalWorkEndTime: Calendar
        get() = TimeUtil.millisToCalendar(normalWorkEndTime)

    /**
     * Calc max. work time based on start time
     *
     * @return Max. work end time
     */
    val maxWorkEndTime: Long
        get() = TimeUtil.getMaxWorkEndTime(mContext, timeStart, timeWorked, homeOffice)

    /**
     * Calc max. work time based on start time
     *
     * @return Max. work end time (Calendar class)
     */
    val calMaxWorkEndTime: Calendar
        get() = TimeUtil.millisToCalendar(maxWorkEndTime)

    companion object {
        private const val PREFS_NAME = "JobLogData"

        // persistent public work data of TimesWork
        // TODO: @JvmStatic can be removed if everything is migrated to Kotlin
        @JvmStatic
        var workStarted: Boolean = false // true, if work is started

        @JvmStatic
        var timeStart: Long = 0L // current start time in milliseconds

        @JvmStatic
        var timeEnd: Long = 0L // current end time in milliseconds

        @JvmStatic
        var timeWorked: Long = 0L // time already worked that day in milliseconds

        @JvmStatic
        var homeOffice: Boolean = false // true, if work is in home office

        @JvmStatic
        var statisticsDate: Calendar = Calendar.getInstance() // Calendar of current statistics view

        @JvmStatic
        var filterMonth: Int = 0 // view filter month (Jan. = 1), set to 0 to disable

        @JvmStatic
        var filterYear: Int = 0 // view filter year, set to 0 to disable

        /**
         * Get / set the start time of work (Calendar).
         * @return Calendar value of timeStart.
         */
        @JvmStatic
        var calStart: Calendar
            get() = TimeUtil.millisToCalendar(timeStart)
            set(cal) {
                timeStart = cal.timeInMillis
            }

        /**
         * Get / set the end time of work (Calendar).
         * @return Calendar value of timeEnd.
         */
        @JvmStatic
        var calEnd: Calendar
            get() = TimeUtil.millisToCalendar(timeEnd)
            set(cal) {
                timeEnd = cal.timeInMillis
            }

        /**
         * Restoring all data from persistent key-value data
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
         */
        @JvmStatic
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
    }
}
