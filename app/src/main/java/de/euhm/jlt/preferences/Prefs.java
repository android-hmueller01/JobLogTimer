/*
 * @file Prefs.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class to handle preferences
 * @author hmueller
 */
@SuppressWarnings("unused")
public class Prefs {
	private final SharedPreferences mSharedPreferencesSettings;

	/**
	 * Class constructor to get a reference to the SharedPreferences.
	 * @param context The context of the preferences whose values are wanted.
	 */
	public Prefs(Context context) {
		// Gets a SharedPreferences instance that points to the default file
		// that is used by the preference framework in the given context.
		mSharedPreferencesSettings = PreferenceManager
				.getDefaultSharedPreferences(context);
	}

	/* *******************
	 * Data type converter
	 * ******************* */
	
	/**
	 * Get a boolean value from mSharedPreferencesSettings.
	 * @param key The name of the preference to retrieve.
	 * @param defValue Value to return if this preference does not exist.
	 * @return The preference value if it exists, or defValue.
	 */
	private boolean getBoolean(String key, boolean defValue) {
		boolean result = mSharedPreferencesSettings.getBoolean(key, defValue);
		return result;
	}

	/**
	 * Get a integer value from mSharedPreferencesSettings.
	 * @param key The name of the preference to retrieve.
	 * @param defValue Value to return if this preference does not exist.
	 * @return The preference value if it exists, or defValue.
	 */
	private int getInteger(String key, int defValue) {
		String value = mSharedPreferencesSettings.getString(key, "" + defValue);
		// parseInt() returns primitive integer type (int), whereby valueOf
		// returns java.lang.Integer,
		return Integer.parseInt(value);
	}

	/**
	 * Get a float value from mSharedPreferencesSettings.
	 * @param key The name of the preference to retrieve.
	 * @param defValue Value to return if this preference does not exist.
	 * @return The preference value if it exists, or defValue.
	 */
	private float getFloat(String key, float defValue) {
		String value = mSharedPreferencesSettings.getString(key, "" + defValue);
		float result = Float.parseFloat(value);
		return result;
	}

	/**
	 * Get a string value from mSharedPreferencesSettings.
	 * @param key The name of the preference to retrieve.
	 * @param defValue Value to return if this preference does not exist.
	 * @return The preference value if it exists, or defValue.
	 */
	private String getString(String key, String defValue) {
		String value = mSharedPreferencesSettings.getString(key, defValue);
		return value;
	}

	
	/* *************************
	 * Access to all preferences 
	 * ************************* */

	/**
	 * Get the selected normal time to work per day.
	 * @return Normal work time in milliseconds.
	 */
	public long getHoursInMillis() {
		return (long) (getFloat("pref_hours_key", 0f) * 1000 * 60 * 60);
	}

	// @formatter:off
	 /*  Settings are not changed by application, done by PreferenceManager ... 
	public static void setHoursInMillis(long time) {
		SharedPreferences.Editor editor = mSharedPreferencesSettings.edit();
	 	float fTime = (float)time / (1000*60*60);
	 	editor.putString("pref_hours_key", String.valueOf(fTime));
	 	editor.commit(); }
	 */
	 // @formatter:on

	/**
	 * Get the selected maximal time allowed to work per day.
	 * @return Maximal work time in milliseconds.
	 */
	public long getMaxHoursInMillis() {
		return (long) (getFloat("pref_max_hours_key", 0f) * 1000 * 60 * 60);
	}

	/**
	 * Get the switch if break settings are individual or by German law.
	 * @return <b>True</b> if break settings are individual by following after settings.<br>
	 * 		   <b>False</b> to use German law default values (30/45 min.).
	 */
	public boolean getBreakIndividualEnabled() {
		return getBoolean("pref_break_individual_enable_key", false);
	}

	/**
	 * Get the selected break time.
	 * @return Break time in milliseconds.
	 */
	public long getBreakTimeInMillis() {
		return (long) (getInteger("pref_break_time_key", 0) * 1000 * 60);
	}

	/**
	 * Get the switch if break after hours or at fix time is selected.
	 * @return <b>True</b> if break after hours is selected.<br>
	 * 		   <b>False</b> if break at fix time is selected.
	 */
	public boolean getBreakAfterHoursEnabled() {
		return getBoolean("pref_break_after_hours_enable_key", true);
	}

	/**
	 * Get selected break after hours time.
	 * @return Time in milliseconds after that a break must be done.
	 */
	public long getBreakAfterHoursInMillis() {
		return (long) (getFloat("pref_break_after_hours_key", 0f) * 1000 * 60 * 60);
	}

	/**
	 * Get selected break at fix time.
	 * @return Fix time in hours at that a break must be done.
	 */
	public float getBreakAtFixTime() {
		return getFloat("pref_break_atfixtime_key", 0f);
	}

	/**
	 * Get the selection of home office use default setting.
	 * @return <b>True</b> use default setting.<br>
	 * 		   <b>False</b> use last setting.
	 */
	public boolean getHomeOfficeUseDefault() {
		return getBoolean("pref_homeoffice_use_default_key", false);
	}

	/**
	 * Get the selection of home office default setting.
	 * @return <b>True</b> use home office work.<br>
	 * 		   <b>False</b> use bureau work.
	 */
	public boolean getHomeOfficeDefaultSetting() {
		return getBoolean("pref_homeoffice_default_setting_key", false);
	}

	/**
	 * Get the selection of normal work time warning.
	 * @return <b>True</b> if warning after normal work time is selected.<br>
	 * 		   <b>False</b> no normal work time warning selected.
	 */
	public boolean getEndHoursWarnEnabled() {
		return getBoolean("pref_end_hours_warn_key", true);
	}

	/**
	 * Get the selection of maximal work time warning.
	 * @return <b>True</b> if warning after maximal work time is selected.<br> 
	 * 		   <b>False</b> no maximal work time warning selected.
	 */
	public boolean getMaxHoursWarnEnabled() {
		return getBoolean("pref_max_hours_warn_key", true);
	}

	/**
	 * Get time before the maximal work time warning should go off.
	 * @return Time in milliseconds before maximal work time.
	 */
	public long getMaxHoursWarnBeforeInMillis() {
		return (long) (getInteger("pref_max_hours_warn_before_key", 0) * 1000 * 60);
	}

	/**
	 * Get the switch if progress bar shows percentage or hours to work.
	 * @return <b>True:</b> Progress bar shows percentage.<br> 
	 * 		   <b>False:</b> Progress bar shows hours to work. 
	 */
	public boolean getViewPercentEnabled() {
		return getBoolean("pref_view_percent_key", true);
	}

	/**
	 * Get the switch if widget gets updated on low battery state.
	 * @return <b>True:</b> Update even under low battery state.<br> 
	 * 		   <b>False:</b> Stop updating when device is in low battery state. 
	 */
	public boolean getWidgetUpdateOnLowBattery() {
		return getBoolean("pref_widget_update_low_battery_key", false);
	}

}
