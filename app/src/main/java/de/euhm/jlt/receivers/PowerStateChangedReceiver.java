/*
 * @file PowerStateChangedReceiver.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import de.euhm.jlt.dao.TimesWork;
import de.euhm.jlt.preferences.Prefs;
import de.euhm.jlt.utils.Constants;

/**
 * The manifest Receiver is used to detect changes in battery state. 
 * When the system broadcasts a "Battery Low" warning we turn off
 * the passive location updates to conserve battery when the app is
 * in the background. 
 * 
 * When the system broadcasts "Battery OK" to indicate the battery
 * has returned to an okay state, the passive location updates are 
 * resumed.
 * 
 * To test BATTERY_LOW try this:
 * > adb -s device-id shell am broadcast -a android.intent.action.BATTERY_LOW
 * > adb -s device-id shell am broadcast -a android.intent.action.BATTERY_OKAY
 * How to get device id? Get list of connected devices with id's:
 * > ...\adt\sdk\platform-tools\adb devices
 */
public class PowerStateChangedReceiver extends BroadcastReceiver {
	private final String LOG_TAG = PowerStateChangedReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean batteryLow = Intent.ACTION_BATTERY_LOW.equals(intent.getAction());
		Prefs prefs = new Prefs(context);

		if (!prefs.getWidgetUpdateOnLowBattery()) {
			TimesWork timesWork = new TimesWork(context);
			AlarmManager alarmMgr = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			Intent alarmIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, alarmIntent, 0);

			if (batteryLow) {
				// cancel alarm under low battery conditions
				alarmMgr.cancel(pendingIntent);
				Log.v(LOG_TAG, "Widget update alarm canceled, because of low battery state.");
			} else if (timesWork.getWorkStarted()) {
				// work is started and battery state is good again
				// update the widget with an alarm, but do not wake up device ...
				context.sendBroadcast(alarmIntent);
				alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
						SystemClock.elapsedRealtime(),
						Constants.WIDGET_UPDATE_INTERVAL, pendingIntent);
				Log.v(LOG_TAG, "Widget update alarm enabled, because battery state is ok again.");
			}
		}
		/*
		 * PackageManager pm = context.getPackageManager(); ComponentName
		 * passiveLocationReceiver = new ComponentName(context,
		 * PassiveLocationChangedReceiver.class);
		 * 
		 * // Disable the passive location update receiver when the battery
		 * state is low. // Disabling the Receiver will prevent the app from
		 * initiating the background // downloads of nearby locations.
		 * pm.setComponentEnabledSetting(passiveLocationReceiver, batteryLow ?
		 * PackageManager.COMPONENT_ENABLED_STATE_DISABLED :
		 * PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
		 * PackageManager.DONT_KILL_APP);
		 */
	}
}