/*
 * @file AlarmReceiver.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * https://stackoverflow.com/questions/46304839/android-8-0-oreo-alarmmanager-with-broadcast-receiver-and-implicit-broadcast-ban
 */
package de.euhm.jlt.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;

import de.euhm.jlt.MainActivity;
import de.euhm.jlt.R;
import de.euhm.jlt.utils.Constants;

public class AlarmReceiver extends BroadcastReceiver {
	private final String LOG_TAG = AlarmReceiver.class.getSimpleName();

	@Override
	public void onReceive(final Context context, final Intent intent) {
		String action = intent.getAction();
		Log.v(LOG_TAG, "onReceive() intent '" + action + "'");

		// Gets an instance of the NotificationManager service
		NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyMgr == null) {
			Log.e(LOG_TAG, "Error getting notification system service.");
			return;
		}
		// Set notification channel for Oreo and later
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL,
					context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_HIGH);
			notifyMgr.createNotificationChannel(channel);
		}

		if (Constants.RECEIVER_NORMAL_WORK_ALARM.equals(action)) {
			// Builds the notification and issues it.
			notifyMgr.notify(Constants.NOTIFICATION_END_WORK, getNotification(context, true));
		} else if (Constants.RECEIVER_MAX_WORK_ALARM.equals(action)) {
			// Builds the notification and issues it.
			notifyMgr.notify(Constants.NOTIFICATION_END_WORK, getNotification(context, false));
		}
	}

	/**
	 * Returns the {@link NotificationCompat} used as part of the foreground service.
	 */
	private Notification getNotification(Context context, boolean normalEnd) {
		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.setAction(Intent.ACTION_MAIN);
		resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent resultPendingIntent =
				PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// make the broadcast Intent explicit by specifying the receiver class
		Intent endWorkIntent = new Intent(Constants.RECEIVER_START_STOP, null, context, StartStopReceiver.class);
		PendingIntent endWorkPendingIntent =
				PendingIntent.getBroadcast(context, 0, endWorkIntent, 0);

		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL)
						.setSmallIcon(R.drawable.ic_launcher_bw)
						.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_color))
						.setContentTitle(context.getResources().getText(R.string.app_name))
						.setSound(Uri.parse("android.resource://" + context.getPackageName() + File.separator + R.raw.beeps))
						// icon is not shown since Nougat https://stackoverflow.com/questions/41503972/icon-is-not-getting-displayed-in-notification-in-android-nougat
						.addAction(R.drawable.ic_action_stop, context.getResources().getText(R.string.button_end),
								endWorkPendingIntent)
						.setOngoing(false)
						.setContentIntent(resultPendingIntent);
		if (normalEnd) {
			builder.setContentText(context.getResources().getText(R.string.work_time_end_notif_text))
					.setTicker(context.getResources().getText(R.string.work_time_end_ticker))
					.setLights(ContextCompat.getColor(context, R.color.alarm_normal_work_time_led), 500, 1000);
		} else {
			builder.setContentText(context.getResources().getText(R.string.work_time_max_notif_text))
					.setTicker(context.getResources().getText(R.string.work_time_max_ticker))
					.setLights(ContextCompat.getColor(context, R.color.alarm_max_work_time_led), 200, 200);
		}

		return builder.build();
	}

}