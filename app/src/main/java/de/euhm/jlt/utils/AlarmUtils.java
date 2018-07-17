/*
 * @file AlarmUtils.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.utils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import de.euhm.jlt.dao.TimesWork;
import de.euhm.jlt.preferences.Prefs;
import de.euhm.jlt.receivers.AlarmReceiver;

/**
 * Helper class to set JobLogTimer alarms
 * @author hmueller
 */
public class AlarmUtils {
	private final static String LOG_TAG = AlarmUtils.class.getSimpleName();

	/**
	 * Set JobLogTimer alarms for normal and maximal work time, set update widget alarm.
	 * @param context Context of application environment.
	 * @param timesWork Current work times data.
	 */
	public static void setAlarms(Context context, TimesWork timesWork) {
		// see http://android-er.blogspot.de/2010/10/simple-example-of-alarm-service-using.html
		// or http://android-er.blogspot.de/2011/05/using-alarmmanager-to-start-scheduled.html
		Intent alarmIntent;
		PendingIntent pendingIntent;
		//Context context = getApplicationContext();
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Prefs prefs = new Prefs(context);

		if (timesWork.getWorkStarted()) {
			// set alarm
			if (prefs.getEndHoursWarnEnabled()) {
				// set an alarm to normal work hour, use explicit intent by specifying the receiver class
				alarmIntent = new Intent(Constants.RECEIVER_NORMAL_WORK_ALARM);
				alarmIntent.setClass(context, AlarmReceiver.class);
				pendingIntent = PendingIntent.getBroadcast(
						context, 0, alarmIntent, 0);

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
					Log.d(LOG_TAG, "alarmMgr.set(RECEIVER_NORMAL_WORK_ALARM)");
					alarmMgr.set(AlarmManager.RTC_WAKEUP, timesWork.getNormalWorkEndTime(), pendingIntent);
				} else {
					Log.d(LOG_TAG, "alarmMgr.setExactAndAllowWhileIdle(RECEIVER_NORMAL_WORK_ALARM)");
					alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timesWork.getNormalWorkEndTime(), pendingIntent);
				}
			}

			if (prefs.getMaxHoursWarnEnabled()) {
				// set a second alarm to max work hour, use explicit intent by specifying the receiver class
				alarmIntent = new Intent(Constants.RECEIVER_MAX_WORK_ALARM);
				alarmIntent.setClass(context, AlarmReceiver.class);
				pendingIntent = PendingIntent.getBroadcast(
						context, 0, alarmIntent, 0);
				long alarmTime = timesWork.getMaxWorkEndTime() - prefs.getMaxHoursWarnBeforeInMillis();
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
					Log.d(LOG_TAG, "alarmMgr.set(RECEIVER_MAX_WORK_ALARM)");
					alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
				} else {
					Log.d(LOG_TAG, "alarmMgr.setExactAndAllowWhileIdle(RECEIVER_MAX_WORK_ALARM)");
					alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
				}
			}

			// update the widget with an alarm, but do not wake up device ...
			alarmIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
			// set repeating alarm only of we are not in power save mode
			// unfortunately isPowerSaveMode() works only with API >= 21
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				if (pm == null || !pm.isPowerSaveMode() || prefs.getWidgetUpdateOnLowBattery()) {
					alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
							SystemClock.elapsedRealtime(),
							Constants.WIDGET_UPDATE_INTERVAL,
							pendingIntent);
				}
			} else {
				// for below Lollipop we do not know power save mode, so always set alarm :-(
				alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
						SystemClock.elapsedRealtime(),
						Constants.WIDGET_UPDATE_INTERVAL,
						pendingIntent);
			}
		} else {
			// cancel alarm
			alarmIntent = new Intent(Constants.RECEIVER_NORMAL_WORK_ALARM);
			alarmIntent.setClass(context, AlarmReceiver.class);
			pendingIntent = PendingIntent.getBroadcast(
					context, 0, alarmIntent, 0);
	        alarmMgr.cancel(pendingIntent);

			alarmIntent = new Intent(Constants.RECEIVER_MAX_WORK_ALARM);
			alarmIntent.setClass(context, AlarmReceiver.class);
			pendingIntent = PendingIntent.getBroadcast(
					context, 0, alarmIntent, 0);
	        alarmMgr.cancel(pendingIntent);

			alarmIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
	        alarmMgr.cancel(pendingIntent);

			// cancel/remove notification
	        NotificationManager notifyMgr = 
	        		(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	        notifyMgr.cancel(Constants.NOTIFICATION_END_WORK);
		}
	}
}
