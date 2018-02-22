/*
 * @file TimesWork.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.dao;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import de.euhm.jlt.utils.TimeUtil;

/**
 * This class is the model and contains the current work times data.
 * @author hmueller
 */
public class TimesWork extends TimeUtil {
	/*
	 * persistent work data of app
	 */
	private static boolean mWorkStarted = false; // true, if work is started
	private static long mTimeStart = 0; // current start time in milliseconds
	private static long mTimeEnd = 0; // current end time in milliseconds
	private static Calendar mStatisticsDate = Calendar.getInstance(); // Calendar of current statistics view
	private static int mFilterMonth = 0; // view filter month (Jan. = 1), set to 0 to disable
	private static int mFilterYear = 0; // view filter year, set to 0 to disable

	/*
	 * Internal data
	 */
	private static final String PREFS_NAME = "JobLogData";
	private Context mContext;
	
	public TimesWork(Context context) {
		mContext = context;
		if (mTimeStart == 0) {
			// Restore work data from persistent store,
			// if static values are destroyed
			loadTimesWork();
		}
	}


	/*
	 * Interface to all data of TimesWork
	 */
	/**
	 * Return <b>true</b> if work is started, otherwise <b>false</b>.
	 * @return The mWorkStarted.
	 */
	public boolean getWorkStarted() {
		return mWorkStarted;
	}

	/**
	 * Set <b>true</b> if work is started, otherwise <b>false</b>.
	 * @param workStarted The mWorkStarted value to set.
	 */
	public void setWorkStarted(boolean workStarted) {
		mWorkStarted = workStarted;
	}

	/**
	 * Get the start time of work in milliseconds.
	 * @return The mTimeStart value.
	 */
	public long getTimeStart() {
		return mTimeStart;
	}

	/**
	 * Set the start time of work in milliseconds.
	 * @param timeStart The mTimeStart value to set.
	 */
	public void setTimeStart(long timeStart) {
		mTimeStart = timeStart;
	}
	
	/**
	 * Get the start time of work (Calendar).
	 * @return Calendar value of mTimeStart.
	 */
	public Calendar getCalStart() {
		return millisToCalendar(mTimeStart);
	}
	
	/**
	 * Set the start time of work (Calendar).
	 * @param cal The Calendar value to set mTimeStart.
	 */
	public void setCalStart(Calendar cal) {
		mTimeStart = cal.getTimeInMillis();
	}
	
	/**
	 * Get the end time of work in milliseconds
	 * @return the mTimeEnd value
	 */
	public long getTimeEnd() {
		return mTimeEnd;
	}

	/**
	 * Set the end time of work in milliseconds
	 * @param timeEnd the mTimeEnd value to set
	 */
	public void setTimeEnd(long timeEnd) {
		mTimeEnd = timeEnd;
	}
	
	/**
	 * Get the end time of work (Calendar).
	 * @return Calendar value of mTimeEnd.
	 */
	public Calendar getCalEnd() {
		return millisToCalendar(mTimeEnd);
	}

	/**
	 * Set the end time of work (Calendar).
	 * @param cal The Calendar value to set mTimeEnd.
	 */
	public void setCalEnd(Calendar cal) {
		mTimeEnd = cal.getTimeInMillis();
	}

	/**
	 * Get a date string from start time.
	 * @return Date string of mTimeStart.
	 */
	public String getDateString() {
		return formatDateString(mTimeStart);
	}


	/**
	 * Get date of statistics display
	 * @return the mStatisticsDate
	 */
	public Calendar getStatisticsDate() {
		return mStatisticsDate;
	}


	/**
	 * Set date of statistics display
	 * @param statisticsDate the mStatisticsDate to set
	 */
	public void setStatisticsDate(Calendar statisticsDate) {
		mStatisticsDate = statisticsDate;
	}


	/**
	 * Get the filter month
	 * @return the mFilterMonth
	 */
	public int getFilterMonth() {
		return mFilterMonth;
	}


	/**
	 * Set the filter month
	 * @param filterMonth the mFilterMonth to set
	 */
	public void setFilterMonth(int filterMonth) {
		mFilterMonth = filterMonth;
	}


	/**
	 * Get the filter year
	 * @return the mFilterYear
	 */
	public int getFilterYear() {
		return mFilterYear;
	}


	/**
	 * Set the filter year
	 * @param filterYear the mFilterYear to set
	 */
	public void setFilterYear(int filterYear) {
		mFilterYear = filterYear;
	}


	/**
	 * Restoring all data from persistent key-value data
	 */
	public void loadTimesWork() {
		// Restore persistent key-value data
		SharedPreferences prefData = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mTimeStart = prefData.getLong("timeStart", -1);
		mTimeEnd = prefData.getLong("timeEnd", -1);
		mWorkStarted = prefData.getBoolean("workStarted", false);
		long statistics = prefData.getLong("statistics", -1);
		if (statistics == -1) {
			mStatisticsDate = getCurrentTime();
		} else {
			mStatisticsDate.setTimeInMillis(statistics);
		}
		mFilterMonth = prefData.getInt("filterMonth", 0);
		mFilterYear = prefData.getInt("filterYear", 0);

	}
		
	/**
	 * Saving all data in persistent key-value data
	 */
	public void saveTimesWork() {
		// Store persistent key-value data 
		SharedPreferences prefData = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefData.edit();
		editor.putLong("timeStart", mTimeStart);
		editor.putLong("timeEnd", mTimeEnd);
		editor.putBoolean("workStarted", mWorkStarted);
		editor.putLong("statistics", mStatisticsDate.getTimeInMillis());
		editor.putInt("filterMonth", mFilterMonth);
		editor.putInt("filterYear", mFilterYear);
		editor.commit(); // Commit the edits
	}


	/**
	 * Calc normal work time based on start time
	 * 
	 * @return Normal work end time
	 */
	public long getNormalWorkEndTime() {
		return getNormalWorkEndTime(mContext, mTimeStart);
	}
	
	/**
	 * Calc normal work time based on start time
	 * 
	 * @return Normal work end time (Calendar class)
	 */
	public Calendar getCalNormalWorkEndTime() {
		return millisToCalendar(getNormalWorkEndTime(mContext, mTimeStart));
	}
	
	/**
	 * Calc max. work time based on start time
	 * 
	 * @return Max. work end time
	 */
	public long getMaxWorkEndTime() {
		return getMaxWorkEndTime(mContext, mTimeStart);
	}

	/**
	 * Calc max. work time based on start time
	 * 
	 * @return Max. work end time (Calendar class)
	 */
	public Calendar getCalMaxWorkEndTime() {
		return millisToCalendar(getMaxWorkEndTime(mContext, mTimeStart));
	}

	/**
	 * Get worked time of a day (corrected by the break times)
	 * 
	 * @return Worked time in Milliseconds
	 */
	public long getWorkedTime() {
		return getWorkedTime(mContext, mTimeStart, mTimeEnd);
	}

	/**
	 * Get overtime of a working day(corrected by the break times)
	 * 
	 * @return Overtime in Milliseconds
	 */
	public long getOverTime() {
		return getOverTime(mContext, mTimeStart, mTimeEnd);
	}
	
}