/*
 * @file StartStopReceiver.java
 * @author Holger Mueller
 *
 * Global receiver to start/stop working (replaces old services, with did not work with Oreo in all cases)
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.receivers;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import de.euhm.jlt.R;
import de.euhm.jlt.dao.TimesDataSource;
import de.euhm.jlt.dao.TimesWork;
import de.euhm.jlt.preferences.Prefs;
import de.euhm.jlt.utils.AlarmUtils;
import de.euhm.jlt.utils.Constants;
import de.euhm.jlt.utils.TimeUtil;

public class StartStopReceiver extends BroadcastReceiver {
	private final String LOG_TAG = StartStopReceiver.class.getSimpleName();

	@Override
	public void onReceive(final Context context, final Intent intent) {
		String action = intent.getAction();
		Log.v(LOG_TAG, "onReceive() intent '" + action + "'");
		Prefs prefs = new Prefs(context);

		// Construct/load TimesWork DAO from persistent data
		TimesWork timesWork = new TimesWork(context);
		if (timesWork.getWorkStarted()) {
			// End work ...
			if (timesWork.getTimeEnd() == -1) {
				// only use current time, if not set manually
				timesWork.setTimeEnd(TimeUtil.getCurrentTimeInMillis());
			}
			timesWork.setWorkStarted(false);

			// write data to database
			TimesDataSource mDataSource = new TimesDataSource(context);
			mDataSource.open();
			mDataSource.createTimes(timesWork);
			mDataSource.close();

			// Store TimesWork DAO to persistent data
			timesWork.saveTimesWork();

			// cancel alarms and notification
			AlarmUtils.setAlarms(context, timesWork);

			// update views that changes take place
			context.sendBroadcast(new Intent(Constants.RECEIVER_UPDATE_VIEW));

			// update AppWidgetProvider
			context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE));

			Toast.makeText(context, R.string.work_ended, Toast.LENGTH_SHORT).show();
		} else {
			// Start work ...
			timesWork.setWorkStarted(true);
			timesWork.setTimeStart(TimeUtil.getCurrentTimeInMillis());
			timesWork.setTimeEnd(-1);
			if (prefs.getHomeOfficeUseDefault()) {
				// use default home office setting from prefs
				timesWork.setHomeOffice(prefs.getHomeOfficeDefaultSetting());
			} // otherwise do not change old timesWork home office value
			long workedTimeDay = TimeUtil.getFinishedDayWorkTime(context, TimeUtil.getCurrentTime());
			timesWork.setTimeWorked(workedTimeDay);

			// Store TimesWork DAO to persistent data
			timesWork.saveTimesWork();

			// set alarms and notification
			AlarmUtils.setAlarms(context, timesWork);

			// update views that changes take place
			context.sendBroadcast(new Intent(Constants.RECEIVER_UPDATE_VIEW));

			// update AppWidgetProvider
			context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE));

			Toast.makeText(context, R.string.work_started, Toast.LENGTH_SHORT).show();
		}
	}
}
