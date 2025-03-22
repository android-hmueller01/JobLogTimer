/*
 * @file TimeUtil.java
 * @author Holger Mueller
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.utils;

import android.content.Context;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.euhm.jlt.dao.Times;
import de.euhm.jlt.dao.TimesDataSource;
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
	 * Convert milliseconds time to time in minutes modulus days.
	 * @param time Time in milliseconds.
	 * @return time in minutes modulus 24h.
	 */
	public static long getTime24InMinutes(long time) {
		return ((long) getHours24(time) * 60 + getMinutes(time));
	}

	/**
	 * Format time string from time in milliseconds modulus days.
	 * @param time Time in milliseconds.
	 * @return Time string formatted like HH:mm.
	 */
	public static String formatTimeString24(long time) {
		return String.format(Locale.getDefault(), (time < 0 ? "-" : "") + "%02d:%02d",
				getHours24(time), getMinutes(time));
	}

	/**
	 * Format time string from time in milliseconds.
	 * @param time Time in milliseconds.
	 * @return Time string formatted like HHHHH:mm.
	 */
	public static String formatTimeString(long time) {
		return String.format(Locale.getDefault(), (time < 0 ? "-" : "") + "%d:%02d",
				getHours(time), getMinutes(time));
	}

	/**
	 * Format date string from time in milliseconds.
	 * @param time Time in milliseconds.
	 * @return Date string formatted like dd.mm.YYYY.
	 */
	public static String formatDateString(long time) {
		return String.format(Locale.getDefault(), "%1$td.%1$tm.%1$tY", time);
	}

	/**
	 * Calculate normal work time.
	 * @param context The context used for the preferences values.
	 * @param timeStart Start time in milliseconds.
	 * @param timeWorked Time already worked that day in milliseconds.
	 * @param homeOffice true if work is in home office
	 * @return Normal work end time in milliseconds.
	 */
	public static long getNormalWorkEndTime(Context context, long timeStart, long timeWorked, boolean homeOffice) {
		Prefs prefs = new Prefs(context);
		long timeEnd = timeStart + prefs.getHoursInMillis() - timeWorked;
		if (homeOffice) {
			// no breaks needed in home office
		} else if (prefs.getBreakIndividualEnabled()) {
			if (prefs.getBreakAfterHoursEnabled()) {
				// break after fix hours
				if (prefs.getHoursInMillis() > prefs.getBreakAfterHoursInMillis()) {
					timeEnd += prefs.getBreakTimeInMillis();
				}
			} else {
				// break at fix time
				long breakTime = prefs.getBreakTimeInMillis() / (60*1000);
				long breakStartTimeInMinutes = (long)(prefs.getBreakAtFixTime() * 60);
				long breakEndTimeInMinutes = breakStartTimeInMinutes + breakTime;
				long timeOffset = getTimeOffsetInMillis() / (60*1000);
				long timeStartInMinutes = getTime24InMinutes(timeStart) + timeOffset;
				// check if start time is during break time
				if (timeStartInMinutes > breakStartTimeInMinutes &&
						timeStartInMinutes < breakEndTimeInMinutes) {
					// timeStart is during break time
					timeEnd += (breakEndTimeInMinutes - timeStartInMinutes) * 60*1000;
				} else if (timeStartInMinutes <= breakStartTimeInMinutes) {
					// timeStart is before break time
					timeEnd += prefs.getBreakTimeInMillis();
				}
			}
		} else {
			// if break is set to German law
			if (prefs.getHoursInMillis() < Constants.GL_WORK_TIME2) {
				timeEnd += Constants.GL_BREAK_TIME1;
			} else {
				timeEnd += Constants.GL_BREAK_TIME2;
			}
		}
		return timeEnd;
	}

	/**
	 * Calculate maximal work time.
	 * @param context The context used for the preferences values.
	 * @param timeStart Start time in milliseconds.
	 * @param timeWorked Time already worked that day in milliseconds.
	 * @param homeOffice true if work is in home office (no breaks)
	 * @return Maximal work end time in milliseconds.
	 */
	public static long getMaxWorkEndTime(Context context, long timeStart, long timeWorked, boolean homeOffice) {
		Prefs prefs = new Prefs(context);
		long timeEnd = timeStart + prefs.getMaxHoursInMillis() - timeWorked;
		if (homeOffice) {
			// no breaks needed in home office
		} else if (prefs.getBreakIndividualEnabled()) {
			if (prefs.getBreakAfterHoursEnabled()) {
				// break after fix hours
				if (prefs.getMaxHoursInMillis() > prefs.getBreakAfterHoursInMillis()) {
					timeEnd += prefs.getBreakTimeInMillis();
				}
			} else {
				// break at fix time
				long breakTime = prefs.getBreakTimeInMillis() / (60*1000);
				long breakStartTimeInMinutes = (long)(prefs.getBreakAtFixTime() * 60);
				long breakEndTimeInMinutes = breakStartTimeInMinutes + breakTime;
				long timeOffset = getTimeOffsetInMillis() / (60*1000);
				long timeStartInMinutes = getTime24InMinutes(timeStart) + timeOffset;
				// check if start time is during break time
				if (timeStartInMinutes > breakStartTimeInMinutes &&
						timeStartInMinutes < breakEndTimeInMinutes) {
					// timeStart is during break time
					timeEnd += (breakEndTimeInMinutes - timeStartInMinutes) * 60*1000;
				} else if (timeStartInMinutes <= breakStartTimeInMinutes) {
					// timeStart is before break time
					timeEnd += prefs.getBreakTimeInMillis();
				}
			}
		} else {
			// if break is set to German law
			timeEnd += Constants.GL_BREAK_TIME2;
		}
		return timeEnd;
	}

	/**
	 * Get worked time of a day (corrected by the break times).
	 * @param context The context used for the preferences values.
	 * @param timeStart Start time in milliseconds.
	 * @param timeEnd End time in milliseconds.
	 * @param homeOffice true if work is in home office (no breaks)
	 * @return Worked time in milliseconds.
	 */
	public static long getWorkedTime(Context context, long timeStart, long timeEnd, boolean homeOffice) {
		Prefs prefs = new Prefs(context);
		// calc work time based on start time, end time and break time
		long workedTime = timeEnd - timeStart;
		if (workedTime < 0) {
			// correct work time if timeEnd might be before timeStart
			workedTime = 0;
		}
		if (homeOffice) {
			// no brake calculation if work is in home office
			return workedTime;
		}
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
			long timeStartInMinutes = getHours24(timeStart) * 60L + getMinutes(timeStart) +
					getTimeOffsetInMillis() / (60*1000);
			long timeEndInMinutes = getHours24(timeEnd) * 60L + getMinutes(timeEnd) +
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
	 * @param homeOffice true if work is in home office (no breaks)
	 * @return Worked time in Milliseconds.
	 */
	public static long getWorkedTime(Context context, Calendar timeStart, Calendar timeEnd, boolean homeOffice) {
		return getWorkedTime(context, timeStart.getTimeInMillis(), timeEnd.getTimeInMillis(), homeOffice);
	}

	/**
	 * Get overtime of a working day(corrected by the break times).
	 * @param context The context used for the preferences values.
	 * @param timeStart Start time in milliseconds.
	 * @param timeEnd End time  in milliseconds.
	 * @param homeOffice true if work is in home office (no breaks)
	 * @return Overtime in Milliseconds.
	 */
	public static long getOverTime(Context context, long timeStart, long timeEnd, boolean homeOffice) {
		Prefs prefs = new Prefs(context);
		return getWorkedTime(context, timeStart, timeEnd, homeOffice) - prefs.getHoursInMillis();
	}
	
	/**
	 * Get overtime of a working day(corrected by the break times).
	 * @param context The context used for the preferences values.
	 * @param timeStart (Calendar class) Start time.
	 * @param timeEnd (Calendar class) End time.
	 * @param homeOffice true if work is in home office (no breaks)
	 * @return Overtime in Milliseconds.
	 */
	public static long getOverTime(Context context, Calendar timeStart, Calendar timeEnd, boolean homeOffice) {
		return getOverTime(context, timeStart.getTimeInMillis(), timeEnd.getTimeInMillis(), homeOffice);
	}

	/**
	 * Get finished work time (from database) for a selected day.
	 * @param context The context used for the preferences values.
	 * @param calDay (Calendar class) Day to look for.
	 * @return Worked time in Milliseconds.
	 */
	public static long getFinishedDayWorkTime(Context context, Calendar calDay) {
		Calendar calStart = (Calendar) calDay.clone();
		Calendar calEnd = Calendar.getInstance();

		// set start of day
		calStart.set(Calendar.HOUR_OF_DAY, 0);
		calStart.set(Calendar.MINUTE, 0);

		// set end day of day
		calEnd.setTimeInMillis(calStart.getTimeInMillis());
		calEnd.add(Calendar.DAY_OF_MONTH, 1);

		// load data form database
		TimesDataSource db = new TimesDataSource(context);
		List<Times> values = db.getTimeRangeTimes(calStart.getTimeInMillis(), calEnd.getTimeInMillis(), "ASC");
		db.close();

		int cnt = values.size();
		long workedTimeDay = 0;
		for (int i = 0; i < cnt; i++) {
			Times ti = values.get(i);
			workedTimeDay += TimeUtil.getWorkedTime(context, ti.timeStart, ti.timeEnd, ti.homeOffice);
		}

		return workedTimeDay;
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
