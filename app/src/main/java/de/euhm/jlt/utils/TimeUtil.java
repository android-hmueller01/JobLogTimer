/*
 * @name TimeUtil.java
 * @author hmueller
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.utils;

import android.content.Context;

import java.util.Calendar;
import java.util.Locale;

import de.euhm.jlt.preferences.Prefs;

/**
 * Helper class to convert milliseconds time, calculate work and over time
 * @author hmueller
 */
public class TimeUtil {

	/**
	 * Convert time in milliseconds to Calendar.
	 * @param time Time in milliseconds.
	 * @return Time (Calendar class).
	 */
	public static Calendar millisToCalendar(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return cal;
	}

	/**
	 * Get current time (seconds and milliseconds set to zero).
	 * @return current time (Calendar class).
	 */
	public static Calendar getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	/**
	 * Get current time in milliseconds (seconds and milliseconds set to zero).
	 * @return Current time in milliseconds.
	 */
	public static long getCurrentTimeInMillis() {
		Calendar cal = getCurrentTime();
		return cal.getTimeInMillis();
	}

	/**
	 * Get current time offset in milliseconds
	 * depending on time zone and daylight saving.
	 * @return Current time offset in milliseconds.
	 */
	public static long getTimeOffsetInMillis() {
		Calendar cal = Calendar.getInstance();
		long zone = cal.get(Calendar.ZONE_OFFSET);
		long dst = cal.get(Calendar.DST_OFFSET);
		return zone + dst;
	}
	
	/**
	 * Convert millisecond time to minutes modulus hours.
	 * @param time Time in milliseconds.
	 * @return minutes (0 ... 59).
	 */
	public static int getMinutes(long time) {
		return (int) ((Math.abs(time) / (1000 * 60)) % 60);
	}

	/**
	 * Convert milliseconds time to hours modulus days.
	 * @param time Time in milliseconds.
	 * @return hours (0 ... 23).
	 */
	public static int getHours24(long time) {
		return (int) ((Math.abs(time) / (1000 * 60 * 60)) % 24);
	}

	/**
	 * Convert milliseconds time to hours.
	 * @param time Time in milliseconds.
	 * @return hours (0 ... 9999..).
	 */
	public static int getHours(long time) {
		return (int) (Math.abs(time) / (1000 * 60 * 60));
	}

	/**
	 * Format time string from time in milliseconds modulus days.
	 * @param time Time in milliseconds.
	 * @return Time string formated like HH:mm.
	 */
	public static String formatTimeString24(long time) {
		return String.format(Locale.getDefault(), (time < 0 ? "-" : "") + "%02d:%02d",
				getHours24(time), getMinutes(time));
	}

	/**
	 * Format time string from time in milliseconds.
	 * @param time Time in milliseconds.
	 * @return Time string formated like HHHHH:mm.
	 */
	public static String formatTimeString(long time) {
		return String.format(Locale.getDefault(), (time < 0 ? "-" : "") + "%d:%02d",
				getHours(time), getMinutes(time));
	}

	/**
	 * Format date string from time in milliseconds.
	 * @param time Time in milliseconds.
	 * @return Date string formated like dd.mm.YYYY.
	 */
	public static String formatDateString(long time) {
		return String.format(Locale.getDefault(), "%1$td.%1$tm.%1$tY", time);
	}

	/**
	 * Calculate normal work time.
	 * @param context The context used for the preferences values.
	 * @param time Start time in milliseconds.
	 * @return Normal work end time in milliseconds.
	 */
	public static long getNormalWorkEndTime(Context context, long time) {
		long endTime = time;
		Prefs prefs = new Prefs(context);
		if (prefs.getBreakIndividualEnabled()) {
			endTime = time + prefs.getHoursInMillis();
			if ((prefs.getBreakAfterHoursEnabled() &&
					prefs.getHoursInMillis() > prefs.getBreakAfterHoursInMillis()) ||
					(time < prefs.getBreakAtFixTime() && endTime > prefs.getBreakAtFixTime())) {
				endTime += prefs.getBreakTimeInMillis();
			}
		} else {
			// if break is set to German law
			if (prefs.getHoursInMillis() < Constants.GL_WORK_TIME2) {
				endTime = time + prefs.getHoursInMillis() + Constants.GL_BREAK_TIME1;
			} else {
				endTime = time + prefs.getHoursInMillis() + Constants.GL_BREAK_TIME2;
			}
		}
		return endTime;
	}

	/**
	 * Calculate maximal work time.
	 * @param context The context used for the preferences values.
	 * @param time Start time in milliseconds.
	 * @return Maximal work end time in milliseconds.
	 */
	public static long getMaxWorkEndTime(Context context, long time) {
		Prefs prefs = new Prefs(context);
		if (prefs.getBreakIndividualEnabled()) {
			time = time + prefs.getMaxHoursInMillis() + prefs.getBreakTimeInMillis();
		} else {
			// if break is set to German law
			time = time + prefs.getMaxHoursInMillis() + Constants.GL_BREAK_TIME2;
		}
		return time;
	}

	/**
	 * Get worked time of a day (corrected by the break times).
	 * @param context The context used for the preferences values.
	 * @param timeStart Start time in milliseconds.
	 * @param timeEnd End time in milliseconds.
	 * @return Worked time in milliseconds.
	 */
	public static long getWorkedTime(Context context, long timeStart, long timeEnd) {
		Prefs prefs = new Prefs(context);
		// calc work time based on start time, end time and break time
		long workedTime = timeEnd - timeStart;
		long breakTime = prefs.getBreakTimeInMillis();
		if (!prefs.getBreakIndividualEnabled()) {
			// break by German law (30 min. after 6 h, 45 min. after 9 h)
			if (workedTime > Constants.GL_WORK_TIME1) {
				if (workedTime < Constants.GL_WORK_TIME1 + Constants.GL_BREAK_TIME1) {
					workedTime = Constants.GL_WORK_TIME1;
				} else {
					workedTime -= Constants.GL_BREAK_TIME1;
				}
			    if (workedTime > Constants.GL_WORK_TIME2 + (Constants.GL_BREAK_TIME2 - Constants.GL_BREAK_TIME1)) {
			    	workedTime -= Constants.GL_BREAK_TIME2 - Constants.GL_BREAK_TIME1;
			    } else if (workedTime > Constants.GL_WORK_TIME2) {
			    	workedTime = Constants.GL_WORK_TIME2;
			    }
			}
		} else if (prefs.getBreakAfterHoursEnabled()) {
			// break after fix hours
			long breakAfterHours = prefs.getBreakAfterHoursInMillis();
			if (workedTime > breakAfterHours + breakTime) {
				workedTime = workedTime - breakTime;
			} else if (workedTime > breakAfterHours) {
				workedTime = breakAfterHours;
			}
		} else {
			// break at fix time
			long breakStartTimeInMinutes = (long)(prefs.getBreakAtFixTime() * 60);
			long breakEndTimeInMinutes = breakStartTimeInMinutes + breakTime / (60*1000);
			long timeStartInMinutes = getHours24(timeStart) * 60 + getMinutes(timeStart) + 
					getTimeOffsetInMillis() / (60*1000);
			long timeEndInMinutes = getHours24(timeEnd) * 60 + getMinutes(timeEnd) + 
					getTimeOffsetInMillis() / (60*1000);
			// 23.01.2015: new implementation, less complicated ...
			// check if start or end time is during break time
			if (timeStartInMinutes > breakStartTimeInMinutes &&
					timeStartInMinutes < breakEndTimeInMinutes) {
				// timeStart is during break time
				workedTime -= (breakEndTimeInMinutes - timeStartInMinutes) * 60*1000;
			}
			if (timeEndInMinutes > breakStartTimeInMinutes &&
					timeEndInMinutes < breakEndTimeInMinutes) {
				// timeEnd is during break time
				workedTime -= (timeEndInMinutes - breakStartTimeInMinutes) * 60*1000;
			}

			// 19.12.2016: bugfix, wrong calculation for work times outside break time 
			// check if we worked during full break time, part times are calculated above
			if (timeStartInMinutes <= breakStartTimeInMinutes &&
					timeEndInMinutes >= breakEndTimeInMinutes) {
				// use normal break time
				workedTime -= breakTime;
			}

			if (workedTime < 0) {
				// if timeStart and timeEnd is during break time workedTime will be < 0
				// correct that and set workedTime to zero
				workedTime = 0;
			}
		}
		return workedTime;
	}

	/**
	 * Get worked time of a day (corrected by the break times).
	 * @param context The context used for the preferences values.
	 * @param timeStart (Calendar class) Start time.
	 * @param timeEnd (Calendar class) End time.
	 * @return Worked time in Milliseconds.
	 */
	public static long getWorkedTime(Context context, Calendar timeStart, Calendar timeEnd) {
		return getWorkedTime(context, timeStart.getTimeInMillis(), timeEnd.getTimeInMillis());
	}

	/**
	 * Get overtime of a working day(corrected by the break times).
	 * @param context The context used for the preferences values.
	 * @param timeStart Start time in milliseconds.
	 * @param timeEnd End time  in milliseconds.
	 * @return Overtime in Milliseconds.
	 */
	public static long getOverTime(Context context, long timeStart, long timeEnd) {
		Prefs prefs = new Prefs(context);
		return getWorkedTime(context, timeStart, timeEnd) - prefs.getHoursInMillis();
	}
	
	/**
	 * Get overtime of a working day(corrected by the break times).
	 * @param context The context used for the preferences values.
	 * @param timeStart (Calendar class) Start time.
	 * @param timeEnd (Calendar class) End time.
	 * @return Overtime in Milliseconds.
	 */
	public static long getOverTime(Context context, Calendar timeStart, Calendar timeEnd) {
		return getOverTime(context, timeStart.getTimeInMillis(), timeEnd.getTimeInMillis());
	}

	/**
	 * Get calender object from filter values month and year.
	 * @param month Month to filter.
	 * @param year Year to filter.
	 * @return Calendar object build from @month and @year
	 */
	public static Calendar getFilterCal(int month, int year) {
		// get filtered range
		Calendar cal = TimeUtil.getCurrentTime();
		// month - 1, because Calendar month January is 0!
		cal.set(year, month - 1, 1, 0, 0, 0);
		return cal;
	}
}
