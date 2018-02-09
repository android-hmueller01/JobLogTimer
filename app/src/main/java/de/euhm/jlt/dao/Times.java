/**
 * $Id: Times.java 32 2015-01-16 18:51:50Z hmueller $
 * 
 * based on http://www.vogella.com/tutorials/AndroidSQLite/article.html#databasetutorial
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.dao;

import java.util.Calendar;
import java.util.Locale;

import android.os.Bundle;

/**
 * This class is the model and contains the data we will save in the database and show in the user interface.
 * 
 * @author hmueller
 * @version $Rev: 32 $
 */
public class Times {
	private long id;
	private long timeStart;
	private long timeEnd;
	
	static final String TIMES_ID = "Times_id";
	static final String TIMES_TIME_START = "Times_timesStart";
	static final String TIMES_TIME_END = "Times_timeEnd";

	/**
	 * Instantiate new Times class with values.
	 * @param id Unique ID within database
	 * @param timeStart Start work time in milliseconds
	 * @param timeEnd End work time in milliseconds
	 */
	public Times(long id, long timeStart, long timeEnd) {
		this.id = id;
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
	}
	
	/**
	 * Get ID value.
	 * @return Unique ID within database
	 */
	public long getId() {
		return id;
	}

	/**
	 * Set ID value.
	 * @param id Unique ID within database
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Get work start time in milliseconds.
	 * @return Start work time in milliseconds
	 */
	public long getTimeStart() {
		return timeStart;
	}

	/**
	 * Get work start time.
	 * @return Start work time as Calendar object
	 */
	public Calendar getCalStart() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeStart);
		return cal;
	}

	/**
	 * Set work start time in milliseconds.
	 * @param timeStart Start work time in milliseconds
	 */
	public void setTimeStart(long timeStart) {
		this.timeStart = timeStart;
	}

	/**
	 * Get work end time in milliseconds.
	 * @return End work time in milliseconds
	 */
	public long getTimeEnd() {
		return timeEnd;
	}

	/**
	 * Get work end time.
	 * @return End work time as Calendar object
	 */
	public Calendar getCalEnd() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeEnd);
		return cal;
	}

	/**
	 * Set work end time in milliseconds.
	 * @param timeEnd End work time in milliseconds
	 */
	public void setTimeEnd(long timeEnd) {
		this.timeEnd = timeEnd;
	}

	/**
	 * Format a given time to a date string like "DD.MM.YYYY".
	 * @param time Time in milliseconds
	 * @return Date string
	 */
	public String getDateString(long time) {
		return String.format(Locale.getDefault(), "%1$td.%1$tm.%1$tY", time);
	}
	
	/**
	 * Format the current date string like "DD.MM.YYYY".
	 * @return Date string
	 */
	public String getDateString() {
		return getDateString(timeStart);
	}

	/**
	 * Format a given time to a time string like "HH:MM".
	 * @param time Time in milliseconds
	 * @return Time string
	 */
	public String getTimeString(long time) {
		return String.format(Locale.getDefault(),"%tR", time);
	}

	/**
	 * Save internal data to savedInstanceState bundle.
	 * @param savedInstanceState savedInstanceState bundle
	 */
	public void saveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putLong(TIMES_ID, id);
		savedInstanceState.putLong(TIMES_TIME_START, timeStart);
		savedInstanceState.putLong(TIMES_TIME_END, timeEnd);
	}

	/**
	 * Load internal data from savedInstanceState bundle.
	 * Do nothing, if savedInstanceState is null.
	 * @param savedInstanceState savedInstanceState bundle
	 */
	public void loadInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			id = savedInstanceState.getLong(TIMES_ID);
			timeStart = savedInstanceState.getLong(TIMES_TIME_START);
			timeEnd = savedInstanceState.getLong(TIMES_TIME_END);
		}
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		// Good conversion?
		return id + ": " + timeStart + " - " + timeEnd;
	}
}