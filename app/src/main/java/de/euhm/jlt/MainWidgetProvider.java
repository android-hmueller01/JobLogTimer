/*
 * @file MainWidgetProvider.java
 * 
 * based on http://www.vogella.com/tutorials/AndroidSQLite/article.html#databasetutorial
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import de.euhm.jlt.dao.TimesWork;
import de.euhm.jlt.receivers.StartStopReceiver;
import de.euhm.jlt.utils.Constants;
import de.euhm.jlt.utils.TimeUtil;

/**
 * JobLogTimer main widget provider
 * @author hmueller
 */
public class MainWidgetProvider extends AppWidgetProvider {
	private static final String LOG_TAG = MainWidgetProvider.class.getSimpleName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Intent intent;
		PendingIntent pendingIntent;
		int flag = 0;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			// needed starting Android 12 (S = 31)
			flag |= PendingIntent.FLAG_IMMUTABLE;
		}

		Log.v(LOG_TAG, "onUpdate()");
		TimesWork timesWork = new TimesWork(context);
		for (int currentWidgetId : appWidgetIds) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_main);

			// is work started?
			if (timesWork.getWorkStarted()) {
				// update widget info line
				long curTimeMillis = TimeUtil.getCurrentTimeInMillis();
				long workTime = TimeUtil.getWorkedTime(context, timesWork.getTimeStart(), curTimeMillis);
				long overTime = TimeUtil.getOverTime(context, timesWork.getTimeStart(), curTimeMillis);
				remoteViews.setTextViewText(R.id.widget_info_line1,
						TimeUtil.formatTimeString24(workTime));
				remoteViews.setTextViewText(R.id.widget_info_line2, "(" +
						TimeUtil.formatTimeString24(overTime) + ")");
				// update the progress bar (worked time)
				int progress = (int) (100 * (float) (curTimeMillis - timesWork.getTimeStart()) / 
						(float) (timesWork.getNormalWorkEndTime() - timesWork.getTimeStart()));
				//if (progress > 100) progress = 100;
				remoteViews.setProgressBar(R.id.widget_progress_bar, 100, progress, false);
				if (curTimeMillis > timesWork.getNormalWorkEndTime()) {
					if (curTimeMillis > timesWork.getMaxWorkEndTime()) {
						remoteViews.setViewVisibility(R.id.widget_progress_bar_red, View.VISIBLE);
					} else {
						remoteViews.setViewVisibility(R.id.widget_progress_bar_yellow, View.VISIBLE);
						remoteViews.setViewVisibility(R.id.widget_progress_bar_red, View.INVISIBLE);
					}
				} else {
					remoteViews.setViewVisibility(R.id.widget_progress_bar_yellow, View.INVISIBLE);
					remoteViews.setViewVisibility(R.id.widget_progress_bar_red, View.INVISIBLE);
				}
				// set new image for stop action
				remoteViews.setImageViewResource(R.id.widget_button_start_stop, R.drawable.ic_action_stop);
			} else {
				// update widget info line
				remoteViews.setTextViewText(R.id.widget_info_line1, 
						context.getResources().getString(R.string.widget_info_line1));
				remoteViews.setTextViewText(R.id.widget_info_line2, 
						context.getResources().getString(R.string.widget_info_line2));
				// update the progress bar (0%)
				remoteViews.setProgressBar(R.id.widget_progress_bar, 100, 0, false);
				remoteViews.setViewVisibility(R.id.widget_progress_bar_yellow, View.INVISIBLE);
				remoteViews.setViewVisibility(R.id.widget_progress_bar_red, View.INVISIBLE);
				// set new image for start action
				remoteViews.setImageViewResource(R.id.widget_button_start_stop, R.drawable.ic_action_start);

				AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flag);
		        if (alarmMgr != null) alarmMgr.cancel(pendingIntent);
			}
			// add onClickListener for Start/Stop
			intent = new Intent(Constants.RECEIVER_START_STOP);
			// make the broadcast Intent explicit by specifying the receiver class
			intent.setClass(context, StartStopReceiver.class);
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flag);
			remoteViews.setOnClickPendingIntent(R.id.widget_button_start_stop, pendingIntent);
			// add onClickListener for Main App
			intent = new Intent(context, MainActivity.class);
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			pendingIntent = PendingIntent.getActivity(context,
					0, intent, flag | PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.widget_button_StartApp, pendingIntent);
			// finally update the widget
			appWidgetManager.updateAppWidget(currentWidgetId, remoteViews);

			// BEWARE: Toast keeps device awake, as it counts as action and this is called cyclic!!!
			//Toast.makeText(context, "widget onUpdate id=" + currentWidgetId, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.v(LOG_TAG, "onReceive() intent '" + action + "'");
		if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
			AppWidgetManager appWM = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWM.getAppWidgetIds(new ComponentName(context, MainWidgetProvider.class));
			this.onUpdate(context, appWM, appWidgetIds);
		} else {
			super.onReceive(context, intent);
		}
	}

	@Override
	public void onDisabled(Context context) {
		Log.v(LOG_TAG, "onDisabled()");

		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		Log.v(LOG_TAG, "onEnabled()");

		super.onEnabled(context);
	}
}
