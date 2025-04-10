/*
 * @file Times.kt
 * 
 * based on http://www.vogella.com/tutorials/AndroidSQLite/article.html#databasetutorial
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.dao

import android.os.Bundle
import java.util.Calendar
import java.util.Locale

/**
 * This class is the Times model and contains the data we will save in the database and show in the user interface.
 *
 * @author hmueller
 *
 * @param id Unique ID within database
 * @param timeStart Start work time in milliseconds
 * @param timeEnd End work time in milliseconds
 * @param homeOffice True if work is in home office
 */
class Times(@JvmField var id: Long,
            @JvmField var timeStart: Long,
            @JvmField var timeEnd: Long,
            @JvmField var homeOffice: Boolean) {

    /**
     * Get work start time.
     * @return Start work time as Calendar object
     */
    val calStart: Calendar
        get() {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timeStart
            return cal
        }

    /**
     * Get work end time.
     * @return End work time as Calendar object
     */
    @Suppress("unused")
    val calEnd: Calendar
        get() {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timeEnd
            return cal
        }

    /**
     * Format a given time to a date string like "DD.MM.YYYY".
     * @param time Time in milliseconds
     * @return Date string
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getDateString(time: Long): String {
        return String.format(Locale.getDefault(), "%1\$td.%1\$tm.%1\$tY", time)
    }

    /**
     * Format the current date string like "DD.MM.YYYY".
     * @return Date string
     */
    val dateString: String
        get() = getDateString(timeStart)

    /**
     * Format a given time to a time string like "HH:MM".
     * @param time Time in milliseconds
     * @return Time string
     */
    @Suppress("unused")
    fun getTimeString(time: Long): String {
        return String.format(Locale.getDefault(), "%tR", time)
    }

    /**
     * Save internal data to savedInstanceState bundle.
     * @param savedInstanceState savedInstanceState bundle
     */
    fun saveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(TIMES_ID, id)
        savedInstanceState.putLong(TIMES_TIME_START, timeStart)
        savedInstanceState.putLong(TIMES_TIME_END, timeEnd)
        savedInstanceState.putBoolean(TIMES_HOME_OFFICE, homeOffice)
    }

    /**
     * Load internal data from savedInstanceState bundle.
     * Do nothing, if savedInstanceState is null.
     * @param savedInstanceState savedInstanceState bundle
     */
    fun loadInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            id = savedInstanceState.getLong(TIMES_ID)
            timeStart = savedInstanceState.getLong(TIMES_TIME_START)
            timeEnd = savedInstanceState.getLong(TIMES_TIME_END)
            homeOffice = savedInstanceState.getBoolean(TIMES_HOME_OFFICE)
        }
    }

    // Will be used by the ArrayAdapter in the ListView
    override fun toString(): String {
        // Good conversion?
        return "$id: $timeStart - $timeEnd"
    }

    companion object {
        private const val TIMES_ID = "Times_id"
        private const val TIMES_TIME_START = "Times_timesStart"
        private const val TIMES_TIME_END = "Times_timeEnd"
        private const val TIMES_HOME_OFFICE = "Times_homeOffice"
    }
}