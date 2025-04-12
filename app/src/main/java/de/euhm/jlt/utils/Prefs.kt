/**
 * @file Prefs.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */

package de.euhm.jlt.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * Helper class to handle preferences
 * @author hmueller
 */
@Suppress("SameParameterValue")
class Prefs(context: Context) {
    // Gets a SharedPreferences instance that points to the default file
    // that is used by the preference framework in the given context.
    private val mSharedPreferencesSettings: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Get a boolean value from mSharedPreferencesSettings.
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return The preference value if it exists, or defValue.
     */
    private fun getBoolean(key: String, defValue: Boolean): Boolean {
        return mSharedPreferencesSettings.getBoolean(key, defValue)
    }

    /**
     * Get a integer value from mSharedPreferencesSettings.
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return The preference value if it exists, or defValue.
     */
    private fun getInteger(key: String, defValue: Int): Int {
        val value = mSharedPreferencesSettings.getString(key, "" + defValue)!!
        // parseInt() returns primitive integer type (int), whereby valueOf
        // returns java.lang.Integer,
        return value.toInt()
    }

    /**
     * Get a float value from mSharedPreferencesSettings.
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return The preference value if it exists, or defValue.
     */
    private fun getFloat(key: String, defValue: Float): Float {
        val value = mSharedPreferencesSettings.getString(key, "" + defValue)!!
        return value.toFloat()
    }

    /**
     * Get a string value from mSharedPreferencesSettings.
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return The preference value if it exists, or defValue.
     */
    @Suppress("unused")
    private fun getString(key: String, defValue: String): String? {
        return mSharedPreferencesSettings.getString(key, defValue)
    }

    /**
     * Get the selected normal time to work per day.
     * @return Normal work time in milliseconds.
     */
    val hoursInMillis: Long
        get() = (getFloat("pref_hours_key", 0f) * 1000 * 60 * 60).toLong()

    /**
     * Get the selected maximal time allowed to work per day.
     * @return Maximal work time in milliseconds.
     */
    val maxHoursInMillis: Long
        get() = (getFloat("pref_max_hours_key", 0f) * 1000 * 60 * 60).toLong()

    /**
     * Get the switch if break settings are individual or by German law.
     * @return **True** if break settings are individual by following after settings.<br></br>
     * **False** to use German law default values (30/45 min.).
     */
    val breakIndividualEnabled: Boolean
        get() = getBoolean("pref_break_individual_enable_key", false)

    /**
     * Get the selected break time.
     * @return Break time in milliseconds.
     */
    val breakTimeInMillis: Long
        get() = getInteger("pref_break_time_key", 0) * 1000L * 60L

    /**
     * Get the switch if break after hours or at fix time is selected.
     * @return **True** if break after hours is selected.<br></br>
     * **False** if break at fix time is selected.
     */
    val breakAfterHoursEnabled: Boolean
        get() = getBoolean("pref_break_after_hours_enable_key", true)

    /**
     * Get selected break after hours time.
     * @return Time in milliseconds after that a break must be done.
     */
    val breakAfterHoursInMillis: Long
        get() = (getFloat("pref_break_after_hours_key", 0f) * 1000 * 60 * 60).toLong()

    /**
     * Get selected break at fix time.
     * @return Fix time in hours at that a break must be done.
     */
    val breakAtFixTime: Float
        get() = getFloat("pref_break_atfixtime_key", 0f)

    /**
     * Get the selection of home office use default setting.
     * @return **True** use default setting.<br></br>
     * **False** use last setting.
     */
    val homeOfficeUseDefault: Boolean
        get() = getBoolean("pref_homeoffice_use_default_key", false)

    /**
     * Get the selection of home office default setting.
     * @return **True** use home office work.<br></br>
     * **False** use bureau work.
     */
    val homeOfficeDefaultSetting: Boolean
        get() = getBoolean("pref_homeoffice_default_setting_key", false)

    /**
     * Get the selection of normal work time warning.
     * @return **True** if warning after normal work time is selected.<br></br>
     * **False** no normal work time warning selected.
     */
    val endHoursWarnEnabled: Boolean
        get() = getBoolean("pref_end_hours_warn_key", true)

    /**
     * Get the selection of maximal work time warning.
     * @return **True** if warning after maximal work time is selected.<br></br>
     * **False** no maximal work time warning selected.
     */
    val maxHoursWarnEnabled: Boolean
        get() = getBoolean("pref_max_hours_warn_key", true)

    /**
     * Get time before the maximal work time warning should go off.
     * @return Time in milliseconds before maximal work time.
     */
    val maxHoursWarnBeforeInMillis: Long
        get() = getInteger("pref_max_hours_warn_before_key", 0) * 1000L * 60L

    /**
     * Get the switch if progress bar shows percentage or hours to work.
     * @return **True:** Progress bar shows percentage.<br></br>
     * **False:** Progress bar shows hours to work.
     */
    val viewPercentEnabled: Boolean
        get() = getBoolean("pref_view_percent_key", true)

    /**
     * Get the switch if widget gets updated on low battery state.
     * @return **True:** Update even under low battery state.<br></br>
     * **False:** Stop updating when device is in low battery state.
     */
    val widgetUpdateOnLowBattery: Boolean
        get() = getBoolean("pref_widget_update_low_battery_key", false)
}
