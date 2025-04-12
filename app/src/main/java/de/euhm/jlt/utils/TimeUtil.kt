/**
 * @file TimeUtil.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.utils

import android.content.Context
import de.euhm.jlt.dao.TimesDataSource
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

/**
 * Helper class to convert milliseconds time, calculate work and over time
 *
 * @author hmueller
 */
object TimeUtil { // create a singleton (static) class
    /**
     * Convert time in milliseconds to Calendar.
     * @param time Time in milliseconds.
     * @return Time (Calendar class).
     */
    @JvmStatic
    fun millisToCalendar(time: Long): Calendar {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        return cal
    }

    /**
     * Get current time (seconds and milliseconds set to zero).
     * @return current time (Calendar class).
     */
    @JvmStatic
    fun getCurrentTime(): Calendar {
        val cal = Calendar.getInstance()
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal
    }

    /**
     * Get current time in milliseconds (seconds and milliseconds set to zero).
     * @return Current time in milliseconds.
     */
    @JvmStatic
    fun getCurrentTimeInMillis(): Long {
        val cal = getCurrentTime()
        return cal.timeInMillis
    }

    /**
     * Get current time offset in milliseconds
     * depending on time zone and daylight saving.
     * @return Current time offset in milliseconds.
     */
    @JvmStatic
    fun getTimeOffsetInMillis(): Long {
        val cal = Calendar.getInstance()
        val zone = cal[Calendar.ZONE_OFFSET].toLong()
        val dst = cal[Calendar.DST_OFFSET].toLong()
        return zone + dst
    }

    /**
     * Convert millisecond time to minutes modulus hours.
     * @param time Time in milliseconds.
     * @return minutes (0 ... 59).
     */
    @JvmStatic
    fun getMinutes(time: Long): Int {
        return ((abs(time.toDouble()) / (1000 * 60)) % 60).toInt()
    }

    /**
     * Convert milliseconds time to hours modulus days.
     * @param time Time in milliseconds.
     * @return hours (0 ... 23).
     */
    @JvmStatic
    fun getHours24(time: Long): Int {
        return ((abs(time.toDouble()) / (1000 * 60 * 60)) % 24).toInt()
    }

    /**
     * Convert milliseconds time to hours.
     * @param time Time in milliseconds.
     * @return hours (0 ... 9999..).
     */
    @JvmStatic
    fun getHours(time: Long): Int {
        return (abs(time.toDouble()) / (1000 * 60 * 60)).toInt()
    }

    /**
     * Convert milliseconds time to time in minutes modulus days.
     * @param time Time in milliseconds.
     * @return time in minutes modulus 24h.
     */
    @JvmStatic
    fun getTime24InMinutes(time: Long): Long {
        return (getHours24(time).toLong() * 60 + getMinutes(time))
    }

    /**
     * Format time string from time in milliseconds modulus days.
     * @param time Time in milliseconds.
     * @return Time string formatted like HH:mm.
     */
    @JvmStatic
    fun formatTimeString24(time: Long): String {
        return String.format(Locale.getDefault(), (if (time < 0) "-" else "") + "%02d:%02d",
            getHours24(time), getMinutes(time))
    }

    /**
     * Format time string from time in milliseconds.
     * @param time Time in milliseconds.
     * @return Time string formatted like HHHHH:mm.
     */
    @JvmStatic
    fun formatTimeString(time: Long): String {
        return String.format(Locale.getDefault(), (if (time < 0) "-" else "") + "%d:%02d",
            getHours(time), getMinutes(time))
    }

    /**
     * Format date string from time in milliseconds.
     * @param time Time in milliseconds.
     * @return Date string formatted like dd.mm.YYYY.
     */
    @Suppress("unused")
    @JvmStatic
    fun formatDateString(time: Long): String {
        return String.format(Locale.getDefault(), "%1\$td.%1\$tm.%1\$tY", time)
    }

    /**
     * Calculate normal work time.
     * @param context The context used for the preferences values.
     * @param timeStart Start time in milliseconds.
     * @param timeWorked Time already worked that day in milliseconds.
     * @param homeOffice true if work is in home office
     * @return Normal work end time in milliseconds.
     */
    @JvmStatic
    fun getNormalWorkEndTime(context: Context, timeStart: Long, timeWorked: Long, homeOffice: Boolean): Long {
        val prefs = Prefs(context)
        var timeEnd = timeStart + prefs.hoursInMillis - timeWorked
        if (homeOffice) {
            // no breaks needed in home office
        } else if (prefs.breakIndividualEnabled) {
            if (prefs.breakAfterHoursEnabled) {
                // break after fix hours
                if (prefs.hoursInMillis > prefs.breakAfterHoursInMillis) {
                    timeEnd += prefs.breakTimeInMillis
                }
            } else {
                // break at fix time
                val breakTime = prefs.breakTimeInMillis / (60 * 1000)
                val breakStartTimeInMinutes = (prefs.breakAtFixTime * 60).toLong()
                val breakEndTimeInMinutes = breakStartTimeInMinutes + breakTime
                val timeOffset = getTimeOffsetInMillis() / (60 * 1000)
                val timeStartInMinutes = getTime24InMinutes(timeStart) + timeOffset
                // check if start time is during break time
                if (timeStartInMinutes in (breakStartTimeInMinutes + 1)..<breakEndTimeInMinutes) {
                    // timeStart is during break time
                    timeEnd += (breakEndTimeInMinutes - timeStartInMinutes) * 60 * 1000
                } else if (timeStartInMinutes <= breakStartTimeInMinutes) {
                    // timeStart is before break time
                    timeEnd += prefs.breakTimeInMillis
                }
            }
        } else {
            // if break is set to German law
            timeEnd += if (prefs.hoursInMillis < Constants.GL_WORK_TIME2) {
                Constants.GL_BREAK_TIME1
            } else {
                Constants.GL_BREAK_TIME2
            }
        }
        return timeEnd
    }

    /**
     * Calculate maximal work time.
     * @param context The context used for the preferences values.
     * @param timeStart Start time in milliseconds.
     * @param timeWorked Time already worked that day in milliseconds.
     * @param homeOffice true if work is in home office (no breaks)
     * @return Maximal work end time in milliseconds.
     */
    @JvmStatic
    fun getMaxWorkEndTime(context: Context, timeStart: Long, timeWorked: Long, homeOffice: Boolean): Long {
        val prefs = Prefs(context)
        var timeEnd = timeStart + prefs.maxHoursInMillis - timeWorked
        if (homeOffice) {
            // no breaks needed in home office
        } else if (prefs.breakIndividualEnabled) {
            if (prefs.breakAfterHoursEnabled) {
                // break after fix hours
                if (prefs.maxHoursInMillis > prefs.breakAfterHoursInMillis) {
                    timeEnd += prefs.breakTimeInMillis
                }
            } else {
                // break at fix time
                val breakTime = prefs.breakTimeInMillis / (60 * 1000)
                val breakStartTimeInMinutes = (prefs.breakAtFixTime * 60).toLong()
                val breakEndTimeInMinutes = breakStartTimeInMinutes + breakTime
                val timeOffset = getTimeOffsetInMillis() / (60 * 1000)
                val timeStartInMinutes = getTime24InMinutes(timeStart) + timeOffset
                // check if start time is during break time
                if (timeStartInMinutes in (breakStartTimeInMinutes + 1)..<breakEndTimeInMinutes) {
                    // timeStart is during break time
                    timeEnd += (breakEndTimeInMinutes - timeStartInMinutes) * 60 * 1000
                } else if (timeStartInMinutes <= breakStartTimeInMinutes) {
                    // timeStart is before break time
                    timeEnd += prefs.breakTimeInMillis
                }
            }
        } else {
            // if break is set to German law
            timeEnd += Constants.GL_BREAK_TIME2
        }
        return timeEnd
    }

    /**
     * Get worked time of a day (corrected by the break times).
     * @param context The context used for the preferences values.
     * @param timeStart Start time in milliseconds.
     * @param timeEnd End time in milliseconds.
     * @param homeOffice true if work is in home office (no breaks)
     * @return Worked time in milliseconds.
     */
    @JvmStatic
    fun getWorkedTime(context: Context, timeStart: Long, timeEnd: Long, homeOffice: Boolean): Long {
        val prefs = Prefs(context)
        // calc work time based on start time, end time and break time
        var workedTime = timeEnd - timeStart
        if (workedTime < 0) {
            // correct work time if timeEnd might be before timeStart
            workedTime = 0
        }
        if (homeOffice) {
            // no brake calculation if work is in home office
            return workedTime
        }
        val breakTime = prefs.breakTimeInMillis
        if (!prefs.breakIndividualEnabled) {
            // break by German law (30 min. after 6 h, 45 min. after 9 h)
            if (workedTime > Constants.GL_WORK_TIME1) {
                if (workedTime < Constants.GL_WORK_TIME1 + Constants.GL_BREAK_TIME1) {
                    workedTime = Constants.GL_WORK_TIME1
                } else {
                    workedTime -= Constants.GL_BREAK_TIME1
                }
                if (workedTime > Constants.GL_WORK_TIME2 + (Constants.GL_BREAK_TIME2 - Constants.GL_BREAK_TIME1)) {
                    workedTime -= Constants.GL_BREAK_TIME2 - Constants.GL_BREAK_TIME1
                } else if (workedTime > Constants.GL_WORK_TIME2) {
                    workedTime = Constants.GL_WORK_TIME2
                }
            }
        } else if (prefs.breakAfterHoursEnabled) {
            // break after fix hours
            val breakAfterHours = prefs.breakAfterHoursInMillis
            if (workedTime > breakAfterHours + breakTime) {
                workedTime -= breakTime
            } else if (workedTime > breakAfterHours) {
                workedTime = breakAfterHours
            }
        } else {
            // break at fix time
            val breakStartTimeInMinutes = (prefs.breakAtFixTime * 60).toLong()
            val breakEndTimeInMinutes = breakStartTimeInMinutes + breakTime / (60 * 1000)
            val timeStartInMinutes =
                getHours24(timeStart) * 60L + getMinutes(timeStart) + getTimeOffsetInMillis() / (60 * 1000)
            val timeEndInMinutes =
                getHours24(timeEnd) * 60L + getMinutes(timeEnd) + getTimeOffsetInMillis() / (60 * 1000)
            // 23.01.2015: new implementation, less complicated ...
            // check if start or end time is during break time
            if (timeStartInMinutes in (breakStartTimeInMinutes + 1)..<breakEndTimeInMinutes) {
                // timeStart is during break time
                workedTime -= (breakEndTimeInMinutes - timeStartInMinutes) * 60 * 1000
            }
            if (timeEndInMinutes in (breakStartTimeInMinutes + 1)..<breakEndTimeInMinutes) {
                // timeEnd is during break time
                workedTime -= (timeEndInMinutes - breakStartTimeInMinutes) * 60 * 1000
            }

            // 19.12.2016: bugfix, wrong calculation for work times outside break time
            // check if we worked during full break time, part times are calculated above
            if (timeStartInMinutes <= breakStartTimeInMinutes &&
                timeEndInMinutes >= breakEndTimeInMinutes) {
                // use normal break time
                workedTime -= breakTime
            }

            if (workedTime < 0) {
                // if timeStart and timeEnd is during break time workedTime will be < 0
                // correct that and set workedTime to zero
                workedTime = 0
            }
        }
        return workedTime
    }

    /**
     * Get worked time of a day (corrected by the break times).
     * @param context The context used for the preferences values.
     * @param timeStart (Calendar class) Start time.
     * @param timeEnd (Calendar class) End time.
     * @param homeOffice true if work is in home office (no breaks)
     * @return Worked time in Milliseconds.
     */
    @JvmStatic
    fun getWorkedTime(context: Context, timeStart: Calendar, timeEnd: Calendar, homeOffice: Boolean): Long {
        return getWorkedTime(context, timeStart.timeInMillis, timeEnd.timeInMillis, homeOffice)
    }

    /**
     * Get worked time of a day (corrected by the break times).
     * @param context The context used for the preferences values.
     * @param timeStart Start time in milliseconds.
     * @param timeEnd End time in milliseconds.
     * @param homeOffice true if work is in home office (no breaks)
     * @param timeWorked Time already worked that day in milliseconds.
     * @return Worked time in milliseconds.
     */
    fun getWorkedTime(context: Context, timeStart: Long, timeEnd: Long, homeOffice: Boolean, timeWorked: Long): Long {
        return getWorkedTime(context, timeStart, timeEnd, homeOffice) + timeWorked
    }

    /**
     * Get overtime of a working day (corrected by the break times).
     * @param context The context used for the preferences values.
     * @param timeStart Start time in milliseconds.
     * @param timeEnd End time  in milliseconds.
     * @param homeOffice true if work is in home office (no breaks)
     * @return Overtime in Milliseconds.
     */
    @JvmStatic
    fun getOverTime(context: Context, timeStart: Long, timeEnd: Long, homeOffice: Boolean): Long {
        val prefs = Prefs(context)
        return getWorkedTime(context, timeStart, timeEnd, homeOffice) - prefs.hoursInMillis
    }

    /**
     * Get overtime of a working day(corrected by the break times).
     * @param context The context used for the preferences values.
     * @param timeStart (Calendar class) Start time.
     * @param timeEnd (Calendar class) End time.
     * @param homeOffice true if work is in home office (no breaks)
     * @return Overtime in Milliseconds.
     */
    @JvmStatic
    fun getOverTime(context: Context, timeStart: Calendar, timeEnd: Calendar, homeOffice: Boolean): Long {
        return getOverTime(context, timeStart.timeInMillis, timeEnd.timeInMillis, homeOffice)
    }

    /**
     * Get overtime of a working day (corrected by the break times).
     * @param context The context used for the preferences values.
     * @param timeStart Start time in milliseconds.
     * @param timeEnd End time  in milliseconds.
     * @param homeOffice true if work is in home office (no breaks)
     * @param timeWorked Time already worked that day in milliseconds.
     * @return Overtime in Milliseconds.
     */
    fun getOverTime(context: Context, timeStart: Long, timeEnd: Long, homeOffice: Boolean, timeWorked: Long): Long {
        val prefs = Prefs(context)
        return getWorkedTime(context, timeStart, timeEnd, homeOffice) + timeWorked - prefs.hoursInMillis
    }

    /**
     * Get finished work time (from database) for a selected day.
     * @param context The context used for the preferences values.
     * @param calDay (Calendar class) Day to look for.
     * @return Worked time in Milliseconds.
     */
    @JvmStatic
    fun getFinishedDayWorkTime(context: Context, calDay: Calendar): Long {
        val calStart = calDay.clone() as Calendar
        val calEnd = Calendar.getInstance()

        // set start of day
        calStart[Calendar.HOUR_OF_DAY] = 0
        calStart[Calendar.MINUTE] = 0

        // set end day of day
        calEnd.timeInMillis = calStart.timeInMillis
        calEnd.add(Calendar.DAY_OF_MONTH, 1)

        // load data form database
        val db = TimesDataSource(context)
        val values = db.getTimeRangeTimes(calStart.timeInMillis, calEnd.timeInMillis, "ASC")
        db.close()

        val cnt = values.size
        var workedTimeDay: Long = 0
        for (i in 0..<cnt) {
            val ti = values[i]
            workedTimeDay += getWorkedTime(context, ti.timeStart, ti.timeEnd, ti.homeOffice)
        }

        return workedTimeDay
    }

    /**
     * Get calender object from filter values month and year.
     * @param month Month to filter.
     * @param year Year to filter.
     * @return Calendar object build from @month and @year
     */
    @JvmStatic
    fun getFilterCal(month: Int, year: Int): Calendar {
        // get filtered range
        val cal = getCurrentTime()
        // month - 1, because Calendar month January is 0!
        cal[year, month - 1, 1, 0, 0] = 0
        return cal
    }
}
