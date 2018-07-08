/*
 * @file AlarmWorkNormalService.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.services;

import java.io.File;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import de.euhm.jlt.MainActivity;
import de.euhm.jlt.R;
import de.euhm.jlt.utils.Constants;

/**
 * Alarm service for normal work time
 * @author hmueller
 */
public class AlarmWorkNormalService extends Service {
	private final String LOG_TAG = AlarmWorkNormalService.class.getSimpleName();
	
	@Override
	public void onCreate() {
		Log.i(LOG_TAG, "onCreate()");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOG_TAG, "onStartCommand()");
		super.onStartCommand(intent, flags, startId);

        // Gets an instance of the NotificationManager service
        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notifyMgr != null) {
			// Builds the notification and issues it.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL,
						getString(R.string.notification_channel), NotificationManager.IMPORTANCE_HIGH);
				notifyMgr.createNotificationChannel(channel);
			}
			notifyMgr.notify(Constants.NOTIFICATION_END_WORK, getNotification());
		}

        // do not restart if service is killed
		return START_NOT_STICKY;
	}

	/**
	 * Returns the {@link NotificationCompat} used as part of the foreground service.
	 */
	private Notification getNotification() {
		Intent resultIntent = new Intent(this, MainActivity.class);
		resultIntent.setAction(Intent.ACTION_MAIN);
		resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent resultPendingIntent =
				PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent endWorkIntent = new Intent(this, EndWorkService.class);
		PendingIntent endWorkPendingIntent =
				PendingIntent.getService(this, 0, endWorkIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL)
						.setSmallIcon(R.drawable.ic_launcher_bw)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_color))
						.setContentTitle(getResources().getText(R.string.app_name))
						.setContentText(getResources().getText(R.string.work_time_end_notif_text))
						.setTicker(getResources().getText(R.string.work_time_end_ticker))
						.setLights(ContextCompat.getColor(this, R.color.alarm_normal_work_time_led), 500, 1000)
						.setSound(Uri.parse("android.resource://" + getPackageName() + File.separator + R.raw.beeps))
						// icon is not shown since Nougat https://stackoverflow.com/questions/41503972/icon-is-not-getting-displayed-in-notification-in-android-nougat
						.addAction(R.drawable.ic_action_stop, getResources().getText(R.string.button_end),
								endWorkPendingIntent)
						.setOngoing(false)
						.setContentIntent(resultPendingIntent);

		return builder.build();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(LOG_TAG, "onBind()");
		return null;
	}

	@Override
	public void onDestroy() {
		Log.i(LOG_TAG, "onDestroy()");
	}
}